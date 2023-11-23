/*************************************************************************
 * Copyright (c) 2020,2021 The Eclipse Foundation and others.
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

public class PackageLockFileReader implements IDependencyListReader {
	final Logger logger = LoggerFactory.getLogger(PackageLockFileReader.class);
	private static final Pattern Name_Pattern = Pattern.compile("(?:(?<scope>@[^\\/]+)\\/)?(?<name>[^\\/]+)$");

	private final InputStream input;

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

			var resolved = value.asJsonObject().getString("resolved", "");
			if (resolved.contains("registry.npmjs.org")) {
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

		public boolean isResolvedLocally() {
			var resolved = value.asJsonObject().getString("resolved", null);
			if (resolved == null)
				return true;
			if (resolved.startsWith("file:"))
				return true;

			if (key.startsWith("packages/"))
				return true;

			if (value.asJsonObject().getBoolean("link", false))
				return true;
			if (value.asJsonObject().getString("version", "").startsWith("file:"))
				return true;
			return false;
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

			return dependencies
					.entrySet()
					.stream()
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
		JsonObject json = JsonUtils.readJson(input);

		switch (json.getJsonNumber("lockfileVersion").intValue()) {
		case 1:
			return new Dependency("", json)
					.stream()
					.filter(each -> !each.key.isEmpty())
					.map(dependency -> dependency.getContentId());
		case 2:
		case 3:
			// @formatter:off
			return json.getJsonObject("packages").entrySet().stream()
					.filter(entry -> !entry.getKey().isEmpty())
					.map(entry -> new Package(entry.getKey(), entry.getValue().asJsonObject()))
					.map(dependency -> dependency.getContentId());
			// @formatter:on
		}

		return Stream.empty();
	}

}
