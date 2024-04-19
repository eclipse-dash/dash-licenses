/*************************************************************************
 * Copyright (c) 2022 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse ids specified in the Package URL format.
 * 
 * <p>
 * The purl format is specified here: https://github.com/package-url/purl-spec
 * 
 * <p>
 * From the documentation (which is licensed under MIT)...
 * 
 * <blockquote>The definition for each components is:
 * <ul>
 * <li><strong>scheme</strong>: this is the URL scheme with the constant value
 * of "pkg". One of the primary reason for this single scheme is to facilitate
 * the future official registration of the "pkg" scheme for package URLs.
 * <em>Required</em>.
 * <li><strong>type</strong>: the package "type" or package "protocol" such as
 * maven, npm, nuget, gem, pypi, etc. <em>Required</em>.
 * <li><strong>namespace</strong>: some name prefix such as a Maven groupid, a
 * Docker image owner, a GitHub user or organization. <em>Optional</em> and
 * type-specific.
 * <li><strong>name</strong>: the name of the package. <em>Required</em>.
 * <li><strong>version</strong>: the version of the package. <em>Optional</em>.
 * <li><strong>qualifiers</strong>: extra qualifying data for a package such as
 * an OS, architecture, a distro, etc. Optional and type-specific.
 * <li><strong>subpath</strong>: extra subpath within a package, relative to the
 * package root. <em>Optional</em>.
 * </ul>
 * </blockquote>
 * 
 * Strictly speaking, the version is considered optional. For our purposes,
 * we've made it mandatory.
 */
public class PackageUrlIdParser implements ContentIdParser {

	// @formatter:off
	private static final String PURL_PATTERN =
			"^(?<scheme>[^:]+:)"
			+ "(?<type>[^\\/]+)"
			+ "(?:\\/(?<group>[^\\/]+))?"
			+ "\\/(?<name>[^@]+)"
			+ "@(?<version>[^?]+)"
			+ "(?<qualifers>\\?[^#]+)?"
			+ "(?<subpath>#.+)?$";
	// @formatter:on

	private static Pattern purlPattern = Pattern.compile(PURL_PATTERN);

	@Override
	public IContentId parseId(String value) {
		Matcher matcher = purlPattern.matcher(value.trim());
		if (!matcher.matches())
			return null;

		var type = matcher.group("type");
		var group = matcher.group("group");
		var name = matcher.group("name");
		var version = matcher.group("version");

		var source = "-";
		if ("maven".equals(type))
			source = "mavencentral";
		if ("npm".equals(type))
			source = "npmjs";
		if ("golang".equals(type)) {
			type = "go";
			source = "golang";
			name = name.replace("/", "%2F");
		}

		if (group == null)
			group = "-";

		return ContentId.getContentId(type, source, group, name, version);
	}
}
