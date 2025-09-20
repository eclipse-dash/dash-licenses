/*************************************************************************
 * Copyright (c) 2019,2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

public class PackageUrlContentId extends ContentId {

	private static final PackageUrlIdParser IdParser = new PackageUrlIdParser();

	private String qualifiers;
	private String subpath;

	private PackageUrlContentId(String type, String source, String namespace, String name, String version, String qualifiers, String subpath) {
		super(type, source, namespace, name, version);
		this.qualifiers = qualifiers;
		this.subpath = subpath;
	}

	public static IContentId getContentId(String type, String source, String namespace, String name, String version, String qualifiers, String subpath) {
		return new PackageUrlContentId(type, source, namespace, name, version, qualifiers, subpath);
	}

	public static IContentId getContentId(String string) {
		return IdParser.parseId(string);
	}

	public String generateDownloadUrl() {
		var label = "download_url=";
		if (qualifiers != null && qualifiers.contains(label)) {
			var q = qualifiers.split("&");
			return q[0].substring(q[0].indexOf(label) + label.length());
		}
		return super.generateDownloadUrl();
	}

	@Override
	public String toString() {
		String ret = "pkg:";
		if (type == "go" || type == "git") {
			ret += source;
		} else {
			ret += type;
		}
		if (namespace != "-") {
			ret += "/" + namespace;
		}
		ret += "/" + name + "@" + version;
		if (qualifiers != null && qualifiers != "") {
			ret += "?" + qualifiers;
		}
		if (subpath != null && subpath != "") {
			ret += "#" + subpath;
		}
		return ret;
	}
}
