/*************************************************************************
 * Copyright (c) 2020 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.InvalidContentId;
import org.eclipse.dash.licenses.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class PackageLockFileReader implements IDependencyListReader {
	final Logger logger = LoggerFactory.getLogger(PackageLockFileReader.class);
	private static final Pattern Name_Pattern = Pattern.compile("(?:(?<scope>@[^\\/]+)\\/)?(?<name>[^\\/]+)$");

	private final InputStream input;
	private JsonObject json;

	public PackageLockFileReader(InputStream input) {
		this.input = input;
	}

	/**
	 * Version 2 of the package-lock.json format includes a flat associative array
	 * of "packages"; each entry contains information about one of the dependencies.
	 * Each instance of this class represents one package entry.
	 */
	class Package {
		final String key;
		final JsonObject value;

		Package(String key, JsonObject value) {
			this.key = key;
			this.value = value;
		}

		Collection<Package> getPackages() {
			/*
			 * More research is needed. AFAICT, it's possible to specify
			 * a relative directory as the key and, my observation is that,
			 * the directory may itself have a package-lock.json file which
			 * would describe additional packages.
			 */
			return Collections.singleton(this);
		}
		
		/**
		 * The content id needs to be extracted from a combination of the key and values
		 * from the associated associative array.
		 * <p>
		 * For version 1, the key contains the name, and--when one is defined--the
		 * scope:
		 * <ul>
		 * <li>junk</li>
		 * <li>@babel/junk</li>
		 * </ul>
		 * 
		 * <p>
		 * For version 2, the key also includes the path:
		 * 
		 * <ul>
		 * <li>node_modules/@types/babel__core</li>
		 * <li>node_modules/@types/babel__generator</li>
		 * <li>node_modules/@jest/transform/node_modules/slash</li>
		 * <li>node_modules/acorn-globals</li>
		 * </ul>
		 * 
		 * <p>
		 * It's relatively easy to handle all cases with a regular expression.
		 */
		IContentId getContentId() {
			var namespace = getNameSpace();
			var name = getName();
			var version = value.asJsonObject().getString("version", null);

			if (version != null) {
				IContentId contentId = ContentId.getContentId("npm", getSource(), namespace, name, version);
				return contentId == null ? new InvalidContentId(key + "@" + version) : contentId;
			}
			return new InvalidContentId(key);
		}

		String getSource() {
			if (isResolvedLocally())
				return "local";

			var resolved = getResolved();
			if (resolved != null && resolved.contains("registry.npmjs.org")) {
				return "npmjs";
			} else {
				logger.debug("Unknown resolved source: {}", resolved);
				return "npmjs";
			}
		}

		String getNameSpace() {
			var name = value.asJsonObject().getString("name", key);
			Matcher matcher = Name_Pattern.matcher(name);
			if (matcher.find()) {
				var scope = matcher.group("scope");
				if (scope != null)
					return scope;
			}

			return "-";
		}

		String getName() {
			var name = value.asJsonObject().getString("name", key);
			Matcher matcher = Name_Pattern.matcher(name);
			if (matcher.find()) {
				return matcher.group("name");
			}

			return null;
		}

		boolean isResolvedLocally() {
			var resolved = getResolved();
			if (resolved == null)
				return false;

			if (resolved.startsWith("file:"))
				return true;

			if (value.asJsonObject().getString("version", "").startsWith("file:"))
				return true;

			return false;
		}

		private String getResolved() {
			return value.asJsonObject().getString("resolved", null);
		}

		private boolean isLink() {
			return value.asJsonObject().getBoolean("link", false);
		}

		public boolean isProjectContent() {
			return isLink() && isInWorkspace(getResolved());
		}

		@Override
		public String toString() {
			return key + " : " + getResolved();
		}
	}

	/**
	 * Version 1 of the package-lock.json format has a notion of dependencies. Each
	 * dependency record contains information about a single resolve dependency.
	 * Every dependency can have its own dependencies (recursive).
	 */
	class Dependency extends Package {

		Dependency(String key, JsonObject value) {
			super(key, value);
		}

		Stream<Dependency> getDependencies() {
			JsonObject dependencies = value.getJsonObject("dependencies");
			if (dependencies == null)
				return Stream.empty();

			return dependencies.entrySet().stream()
					.map(each -> new Dependency(each.getKey(), each.getValue().asJsonObject()));
		}

		/**
		 * Flatten the arbitrary hierarchy (tree) of dependencies into a single stream.
		 */
		Stream<Dependency> stream() {
			return Stream.concat(Stream.of(this), getDependencies().flatMap(Dependency::stream));
		}
	}

	@Override
	public Collection<IContentId> getContentIds() {
		return contentIds().filter(contentId -> "local" != contentId.getSource()).collect(Collectors.toList());
	}

	public Stream<IContentId> contentIds() {
		json = JsonUtils.readJson(input);

		switch (json.getJsonNumber("lockfileVersion").intValue()) {
		case 1:
			return new Dependency("", json).stream().filter(each -> !each.key.isEmpty())
					.map(dependency -> dependency.getContentId());
		case 2:
		case 3:
			// @formatter:off
			return json.getJsonObject("packages").entrySet().stream()
					.filter(entry -> !entry.getKey().isEmpty())
					.map(entry -> new Package(entry.getKey(), entry.getValue().asJsonObject()))
					.flatMap(item -> item.getPackages().stream())
					.filter(item -> !item.isProjectContent())
					.map(dependency -> dependency.getContentId());
			// @formatter:on
		}

		return Stream.empty();
	}

	/**
	 * Answer whether or not a resolved link points to a workspace. This is true
	 * when the link is <code>true</code> and the resolved path matches a workspace
	 * specified in the header.
	 * 
	 * <pre>
	 * ...
	 * "node_modules/vscode-js-profile-core": {
	 *   "resolved": "packages/vscode-js-profile-core",
	 *   "link": true
	 * },
	 * ...
	 * </pre>
	 * 
	 * @param value
	 * @return
	 */
	boolean isInWorkspace(String value) {
		if (value == null) return false;

		return getWorkspaces().anyMatch(each -> glob(each, value));
	}

	private Stream<String> getWorkspaces() {
		return getRootPackage()
				.getOrDefault("workspaces", JsonValue.EMPTY_JSON_ARRAY).asJsonArray()
				.getValuesAs(JsonString.class).stream().map(JsonString::getString);
	}

	/**
	 * The root package is the one that has an empty string as the key. It's not at
	 * all clear to me whether package-lock.json file have more than one package;
	 * more research is required.
	 */
	private JsonObject getRootPackage() {
		return getPackages().entrySet().stream()
				.filter(entry -> entry.getKey().isEmpty())
				.map(entry -> entry.getValue().asJsonObject())
				.findFirst().orElse(JsonValue.EMPTY_JSON_OBJECT);
	}

	private JsonObject getPackages() {
		return json.getOrDefault("packages", JsonValue.EMPTY_JSON_OBJECT).asJsonObject();
	}

	/**
	 * Do a glob match. The build-in function is a little too tightly coupled with
	 * the file system for my liking. This implements a simple translation from glob
	 * to regex that should hopefully suit most of our requirements.
	 */
	boolean glob(String pattern, String value) {
		var regex = pattern.replace("/", "\\/").replace(".", "/.").replace("*", ".*");
		return Pattern.matches(regex, value);
	}
}
