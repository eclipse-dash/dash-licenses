/*************************************************************************
 * Copyright (c) 2025 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

import com.github.packageurl.PackageURL;

public class PackageUrl implements IContentId {
	private PackageURL purl;

	public PackageUrl(PackageURL purl) {
		this.purl = purl;
	}

	@Override
	public String getNamespace() {
		var namespace = purl.getNamespace();
		if (namespace == null) return "-";
		return namespace;
	}

	@Override
	public String getName() {
		return purl.getName();
	}

	@Override
	public String getVersion() {
		return purl.getVersion();
	}

	@Override
	public String getType() {
		var type = purl.getType();
		switch (purl.getType()) {
		case "github": return "git";
		}
		return type;
	}

	@Override
	public String getSource() {
		switch (purl.getType()) {
		case "maven": return "mavencentral";
		case "npm": return "npmjs";
		case "github": return "github";
		}
		
		return "-";
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getClearlyDefinedId() {
		return ContentId.getContentId(getType(), getSource(), getNamespace(), getName(), getVersion()).toString();
	}

	@Override
	public String getPackageUrl() {
		return purl.toString();
	}

	@Override
	public String toString() {
		// FIXME more natural to print the packageURL
		return getClearlyDefinedId();
	}

	public String getDownloadUrl() {
		return purl.getQualifiers().get("download_url");
	}
}
