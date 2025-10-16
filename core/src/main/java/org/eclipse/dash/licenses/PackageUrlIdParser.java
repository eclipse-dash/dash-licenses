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

import java.util.stream.Collectors;

import org.sonatype.goodies.packageurl.InvalidException;
import org.sonatype.goodies.packageurl.PackageUrl;
import org.sonatype.goodies.packageurl.PackageUrlParser;

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
	
	@Override
	public IContentId parseId(String input) {
		PackageUrl packageUrl;
		try {
			packageUrl = new PackageUrlParser().parse(input);
		} catch (InvalidException e) {
			return null;
			
		}
		var type = packageUrl.getType();
		var namespace = packageUrl.getNamespace() != null ? packageUrl.getNamespace().stream().collect(Collectors.joining("%2F")) : "-";
		var name = packageUrl.getName();
		
		// The version is considered optional by the specification/parser. We need a version; when it's
		// missing, we consider the ID to be incomplete/invalid.
		var version = packageUrl.getVersion();
		if (version == null) return null;
		
		var path = packageUrl.getSubpath();
		
		// FIXME This requires more attention. 
		// Interpreting Package URLs is, I believe, an exercise
		// that's left for the reader. We assume, for example, that the source for a
		// "maven" URL is "mavencentral", but that's just the default: the specification 
		// allows for qualifiers to provide a pointer to a specific repository. 
		// We should respect that.
		
		var source = "-";
		if ("github".equals(type)) {
			type = "git";
			source = "github";
		} else if ("maven".equals(type)) {
			source = "mavencentral";
		} else if ("npm".equals(type)) {
			source = "npmjs";
		} else if ("golang".equals(type)) {
			type = "go";
			source = "golang";
			name = name.replace("/", "%2F");
		}
		
		if (path != null && !path.isEmpty()) name += "%23" + path.stream().collect(Collectors.joining("%2F"));
		return ContentId.getContentId(type, source, namespace, name, version);
	}
}
