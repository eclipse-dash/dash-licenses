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

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;

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
	public IContentId parseId(String value) {
		try {
			var purl = new PackageURL(value);
			return new PackageUrl(purl);
		} catch (MalformedPackageURLException e) {
			return null;
		}
	}
}
