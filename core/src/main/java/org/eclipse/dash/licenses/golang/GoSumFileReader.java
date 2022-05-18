/*************************************************************************
 * Copyright (c) 2021 Oleksandr Andriienko
 * Copyright (c) 2022 The Eclipse Foundation
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.golang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.cli.IDependencyListReader;
import org.eclipse.dash.licenses.http.HttpClientService;
import org.eclipse.dash.licenses.util.JsonUtils;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class GoSumFileReader implements IDependencyListReader {

	private final BufferedReader reader;
	private final String githubToken;
	private final Map<String, String> commonHeaders = new HashMap<>();

	private final int requestPageSize = 100;

	@Inject
	protected HttpClientService clientService;

	@Inject
	protected JsoupProvider jsoupProvider;

	@Inject
	public GoSumFileReader(Reader reader) throws IOException {
		this.reader = new BufferedReader(reader);

		// todo notify user, we need to have token to make a lot of GET request to the
		// github api
		githubToken = System.getenv("GITHUB_TOKEN");
		commonHeaders.put("Authorization", "bearer " + githubToken);
	}

	@Override
	public Collection<IContentId> getContentIds() {
		Collection<String> deps = reader.lines().collect(Collectors.toList());
		Collection<GoLangPackage> modulesOrPackages = deps
				.stream()
				.map(this::parsePackage)
				.filter(Objects::nonNull)
				.collect(Collectors
						.toMap(GoLangPackage::getPackageName, (goLangPackage) -> goLangPackage,
								// Get line with the the latest used package version.
								// It will be actual package version which golang uses for compilation.
								(packageName1, packageName2) -> packageName2))
				.values();

		Collection<GoLangPackage> packagesOrModules = modulesOrPackages
				.stream()
				.map(this::setUpSourceInfo)
				.collect(Collectors.toList());

		return packagesOrModules.stream().map(this::convertGoPackageOrModuleToContentId).collect(Collectors.toList());
	}

	private IContentId convertGoPackageOrModuleToContentId(GoLangPackage packageOrModule) {
		System.out.println("Converting " + packageOrModule.packageName);
		String packageIdentifier = packageOrModule.getSourceLocation();

		if (packageIdentifier.endsWith(".git")) {
			int offSet = packageIdentifier.lastIndexOf(".git");
			packageIdentifier = packageIdentifier.substring(0, offSet);
		}
		if (packageIdentifier.matches(".*v[0-9]")) {
			int offSet = packageIdentifier.lastIndexOf("v");
			packageIdentifier = packageIdentifier.substring(0, offSet);
		}

		packageIdentifier = packageIdentifier.replace("https://", "");

		String[] identifierSegments = packageIdentifier.split("/");
		String namespace = identifierSegments[1];
		String name = identifierSegments[2];

		if (packageOrModule.getRevision().matches("v[0-9]+.[0-9]+.[0-9]+(\\+incompatible)?")) {
			packageOrModule.revision = packageOrModule.revision.replace("+incompatible", "");
			String tag = packageOrModule.subPackage != null && !packageOrModule.subPackage.matches("v[0-9]+")
					? packageOrModule.subPackage + "/" + packageOrModule.revision
					: packageOrModule.revision;
			findTagSHA(namespace, name, tag, packageOrModule::setFullSHA, 0);
		} else {
			String shortSHA = packageOrModule.getRevision().split("-")[2];
			getFullSHA(namespace, name, shortSHA, packageOrModule::setFullSHA);
		}

		String version = packageOrModule.getFullSHA() != null ? packageOrModule.getFullSHA()
				: packageOrModule.getRevision();
		return ContentId.getContentId("git", "github", namespace, name, version);
	}

	private void getFullSHA(String org, String repoName, String shortSHA, Consumer<String> consumer) {
		this.clientService
				.get(String.format("https://api.github.com/repos/%s/%s/commits/%s", org, repoName, shortSHA),
						"application/json", commonHeaders, (inputStream) -> {
							JsonObject commit = JsonUtils.readJson(inputStream);
							consumer.accept(commit.get("sha").toString().replace("\"", ""));
						}); // handle error
	}

	private void findTagSHA(String org, String repoName, String tag, Consumer<String> consumer, final int page) {
		try {
			this.clientService
					.get(String
							.format("https://api.github.com/repos/%s/%s/tags?per_page=%d&page=%d", org, repoName,
									requestPageSize, page),
							"application/json", commonHeaders, (inputStream) -> {
								JsonArray tagArray = JsonUtils.readJsonArray(inputStream);
								if (tagArray.isEmpty()) {
									return;
								}
								for (JsonValue jsonTag : tagArray) {
									JsonObject jsoTag = jsonTag.asJsonObject();
									String tagName = jsoTag.get("name").toString().replace("\"", "");
									if (tag.equals(tagName)) {
										consumer
												.accept(jsoTag
														.get("commit")
														.asJsonObject()
														.get("sha")
														.toString()
														.replace("\"", ""));
										return;
									}
								}
								// search tag on the next page
								findTagSHA(org, repoName, tag, consumer, page + 1);
							}); // Todo handle error with try/catch. Error can be 404, 503 or something like
								// that.
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private GoLangPackage setUpSourceInfo(GoLangPackage packageOrModule) {
		try {
			String packageName = packageOrModule.getPackageName();
			System.out.println("====" + packageName);
			if (packageOrModule.getPackageName().startsWith("github.com")) {
				String[] repoSegments = packageName.split("/");
				packageName = repoSegments[0] + "/" + repoSegments[1] + "/" + repoSegments[2];
			}
			// We can apply http support, but it looks dangerously to use such
			// dependencies...
			String goModuleInfoUrl = "https://" + packageName + "?go-get=1";
			Document doc = this.jsoupProvider.getDocument(goModuleInfoUrl);
			retrieveSourceInfoFromMetaTag(packageOrModule, doc, "go-import");
			if (packageOrModule.getSourceLocation() != null
					&& !packageOrModule.getSourceLocation().startsWith("https://github.com")) {
				retrieveSourceInfoFromMetaTag(packageOrModule, doc, "go-source");
			}
		} catch (Exception e) {
			// Todo handle error...
			System.out.println(e);
		}
		return packageOrModule;
	}

	private GoLangPackage retrieveSourceInfoFromMetaTag(GoLangPackage packageOrModule, Document doc,
			String metaTagName) {
		Elements newsHeadlines = doc.select("meta[name='" + metaTagName + "']");
		for (Element headline : newsHeadlines) {
			Optional<Attribute> attribute = headline
					.attributes()
					.asList()
					.stream()
					.filter(attr -> attr.getKey().equals("content"))
					.findFirst();
			if (attribute.isPresent()) {
				String content = attribute.get().getValue().replace("\n", "").replace("\r", "");

				String[] segments = content.split("\\s+");
				String moduleOrPackageName = segments[0];
				String sourceURL = segments[2];

				if (packageOrModule.packageName.startsWith(moduleOrPackageName)
						|| packageOrModule.packageName.startsWith("github.com")) {
					packageOrModule.setSourceLocation(sourceURL);

					String packageRoot = moduleOrPackageName.replaceFirst("(http://)|(https://)", "");
					String[] sourceURLPath = packageRoot.split("/");
					String[] packageURLPath = packageOrModule.packageName.split("/");

					if (sourceURLPath[0].equals(packageURLPath[0]) // If domain is the same
							&& packageURLPath.length > sourceURLPath.length) {
						String[] subPackagePath = Arrays
								.copyOfRange(packageURLPath, sourceURLPath.length, packageURLPath.length);
						String subPackage = String.join("/", subPackagePath);
						packageOrModule.setSubPackage(subPackage);
					}
				}
			}
		}
		return packageOrModule;
	}

	private GoLangPackage parsePackage(String line) {
		Pattern pattern = Pattern.compile("(?<packageName>.*) (?<revision>.*) (?<hash>h1:.*)");
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			String packageName = matcher.group("packageName");
			String revision = matcher.group("revision").replace("/go.mod", "");
			return new GoLangPackage(packageName, revision);
		}
		return null;
	}

	static class GoLangPackage {
		private final String packageName;
		private String revision;
		private String sourceLocation;
		private String subPackage;
		private String fullSHA; // rename to fullCommitSHA...

		public GoLangPackage(String packageName, String revision) {
			this.packageName = packageName;
			this.revision = revision;
		}

		public String getPackageName() {
			return packageName;
		}

		public String getRevision() {
			return revision;
		}

		public String getSourceLocation() {
			return sourceLocation;
		}

		public void setSourceLocation(String sourceLocation) {
			this.sourceLocation = sourceLocation;
		}

		public String getSubPackage() {
			return subPackage;
		}

		public void setSubPackage(String subPackage) {
			this.subPackage = subPackage;
		}

		public String getFullSHA() {
			return fullSHA;
		}

		public void setFullSHA(String fullSHA) {
			this.fullSHA = fullSHA;
		}
	}
}
