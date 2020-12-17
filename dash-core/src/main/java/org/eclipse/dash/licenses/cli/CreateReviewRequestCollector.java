/*************************************************************************
 * Copyright (c) 2020, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.LicenseSupport.Status;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedContentData;

/**
 * The "Create Review Request" collector tracks the results that likely require
 * some review from the IP Team. The gathered information is output to an
 * {@link OutputStream} in Markdown format, suitable for use as a description in
 * a review request.
 */
public class CreateReviewRequestCollector implements IResultsCollector {

	private PrintWriter output;
	private List<LicenseData> needsReview = new ArrayList<>();
	private Project project;

	public CreateReviewRequestCollector(Project project, OutputStream out) {
		this.project = project;
		output = new PrintWriter(out);
	}

	@Override
	public void accept(LicenseData data) {
		if (data.getStatus() != Status.Approved) {
			needsReview.add(data);
		}
	}

	@Override
	public void close() {
		if (needsReview.isEmpty()) {
			output.println(
					"Vetted license information was found for all content. No further investigation is required.");
		} else {
			if (project != null) {
				output.println(String.format("Project: [%s](%s)", project.getName(), project.getUrl()));
				output.println();
			}
			output.println("The following content requires review:");
			output.println();
			needsReview.stream().sorted((a, b) -> a.getId().compareTo(b.getId())).forEach(each -> describe(each));
			output.println();
		}
		output.flush();
	}

	private void describe(LicenseData licenseData) {
		output.println(String.format("* [ ] %s", licenseData.getId()));
		licenseData.contentData().forEach(data -> describeItem(data));
		String searchUrl = IPZillaSearchBuilder.build(licenseData);
		if (searchUrl != null) {
			output.println(String.format("  - [Search IPZilla](%s)", searchUrl));
		}
		IContentId id = licenseData.getId();
		if ("maven".equals(id.getType()) && "mavencentral".equals(id.getSource())) {
			output.println(String.format("  - [Maven Central](https://search.maven.org/artifact/%s/%s/%s/jar)",
					id.getNamespace(), id.getName(), id.getVersion()));
			var source = getMavenSourceUrl(id);
			if (source != null) {
				output.println(String.format("  - [Source](%s) from Maven Central", source));
			}
		}
		if ("npm".equals(id.getType()) && "npmjs".equals(id.getSource())) {
			var builder = new StringBuilder();
			if (!"-".equals(id.getNamespace())) {
				builder.append(id.getNamespace());
				builder.append('/');
			}
			builder.append(id.getName());
			output.println(String.format("  - [npmjs.com](https://www.npmjs.com/package/%s/v/%s)", builder.toString(),
					id.getVersion()));
		}
	}

	/**
	 * THis method writes potentially helpful information to make the intellectual
	 * review process as easy as possible to the output writer.
	 * 
	 * @param data
	 */
	private void describeItem(IContentData data) {
		// FIXME This is clunky

		String authority = data.getAuthority();
		if (data.getUrl() != null)
			authority = String.format("[%s](%s)", authority, data.getUrl());
		output.println(String.format("  - %s %s (%d)", authority, data.getLicense(), data.getScore()));
		switch (data.getAuthority()) {
		case ClearlyDefinedContentData.CLEARLYDEFINED:
			((ClearlyDefinedContentData) data).discoveredLicenses()
					.forEach(license -> output.println("    - " + license));
		};
	}

	@Override
	public int getStatus() {
		return needsReview.size();
	}

	private String getMavenSourceUrl(IContentId id) {
		if (!id.isValid())
			return null;

		// FIXME Validate that this file pattern is correct.
		// This pattern was observed and appears to be accurate.
		var url = "https://search.maven.org/remotecontent?filepath={groupPath}/{artifactid}/{version}/{artifactid}-{version}-sources.jar";
		url = url.replace("{groupPath}", id.getNamespace().replace('.', '/'));
		url = url.replace("{artifactid}", id.getName());
		url = url.replace("{version}", id.getVersion());

		if (remoteFileExists(url)) {
			return url;
		}

		return null;
	}

	private static boolean remoteFileExists(String url) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("HEAD");
			return (connection.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static final class IPZillaSearchBuilder {
		private static final String[] COMMON_TERMS = new String[] { "apache", "eclipse", "source", "platform", "plugin",
				"parent", "client", "server" };
		private static final int SEARCH_TERM_MINIMUM_LENGTH = 5;

		Set<String> terms = new LinkedHashSet<>();

		public static String build(LicenseData licenseData) {
			return new IPZillaSearchBuilder().get(licenseData);
		}

		private void add(String term) {
			// Break the name into segments (non-word characters) and add the segments from
			// the name to the search terms. We arbitrarily decide that terms that are
			// "short" aren't interesting and skip them. The logic being that shorter words
			// are more likely to be common, and common words will clutter up our search.
			if (Arrays.stream(COMMON_TERMS).anyMatch(each -> each.equalsIgnoreCase(term)))
				return;
			if (term.length() >= SEARCH_TERM_MINIMUM_LENGTH)
				terms.add(term);
		}

		private String get(LicenseData licenseData) {
			if (!licenseData.getId().isValid())
				return null;

			String namespace = licenseData.getId().getNamespace();
			String name = licenseData.getId().getName();

			// Assemble terms from the content data that might result
			// in an interesting search.
			terms.add(namespace);
			terms.add(name);

			// Break the name into segments (non-word characters) and add the segments from
			// the name to the search terms.
			for (String segment : name.split("\\W"))
				add(segment);

			for (String segment : namespace.split("\\W"))
				add(segment);

			if (terms.isEmpty())
				return null;

			var builder = new StringBuilder();
			builder.append("https://dev.eclipse.org/ipzilla/buglist.cgi");
			builder.append("?short_desc_type=anywords");
			builder.append("&short_desc=");
			builder.append(String.join("+", terms));
			builder.append("&long_desc_type=substring");

			return builder.toString();
		}
	}
}