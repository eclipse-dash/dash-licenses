/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.context;

import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseChecker;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedSupport;
import org.eclipse.dash.licenses.foundation.EclipseFoundationSupport;
import org.eclipse.dash.licenses.http.HttpClientService;
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.eclipse.dash.licenses.npmjs.INpmjsPackageService;
import org.eclipse.dash.licenses.npmjs.NpmjsPackageService;
import org.eclipse.dash.licenses.review.GitLabSupport;

public class DefaultContext implements IContext {

	private ISettings settings;
	private LicenseChecker licenseCheckerService;
	private GitLabSupport gitlabService;
	private LicenseSupport licenseService;
	private NpmjsPackageService npmjsPackageService;

	public DefaultContext(ISettings settings) {
		this.settings = settings;

		// Initialize basic services
		// TODO we don't always need all of these; consider making initialization lazy.
		licenseCheckerService = new LicenseChecker(this);
		gitlabService = new GitLabSupport(this);
		licenseService = LicenseSupport.getLicenseSupport(this);
		npmjsPackageService = new NpmjsPackageService(this);
	}

	@Override
	public LicenseChecker getLicenseCheckerService() {
		return licenseCheckerService;
	}

	@Override
	public GitLabSupport getGitLabService() {
		return gitlabService;
	}

	@Override
	public ILicenseDataProvider getIPZillaService() {
		return new EclipseFoundationSupport(this);
	}

	@Override
	public ILicenseDataProvider getClearlyDefinedService() {
		return new ClearlyDefinedSupport(this);

	}

	@Override
	public ISettings getSettings() {
		return settings;
	}

	@Override
	public LicenseSupport getLicenseService() {
		return licenseService;
	}

	@Override
	public IHttpClientService getHttpClientService() {
		return new HttpClientService(this);
	}

	@Override
	public INpmjsPackageService getNpmjsService() {
		return npmjsPackageService;
	}
}