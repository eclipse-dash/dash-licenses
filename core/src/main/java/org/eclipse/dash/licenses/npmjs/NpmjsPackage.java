/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.npmjs;

import org.eclipse.dash.licenses.IContentId;

public class NpmjsPackage {

	private IContentId id;
	private String url;
	private String repositoryUrl;
	private String sourceUrl;
	private String license;
	private String distributionUrl;

	public NpmjsPackage(IContentId id) {
		this.id = id;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	public String getUrl() {
		return url;
	}

	void setUrl(String url) {
		this.url = url;
	}

	public IContentId getId() {
		return id;
	}

	public String getLicense() {
		return license;
	}

	void setLicense(String license) {
		this.license = license;
	}

	public String getDistributionUrl() {
		return distributionUrl;
	}

	void setDistributionUrl(String distributionUrl) {
		this.distributionUrl = distributionUrl;
	}
}
