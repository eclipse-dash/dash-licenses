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
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.eclipse.dash.licenses.npmjs.INpmjsPackageService;
import org.eclipse.dash.licenses.review.GitLabSupport;

public interface IContext {

	default LicenseChecker getLicenseCheckerService() {
		return null;
	};

	default GitLabSupport getGitLabService() {
		return null;
	};

	default ILicenseDataProvider getIPZillaService() {
		return null;
	};

	default ILicenseDataProvider getClearlyDefinedService() {
		return null;
	};

	default LicenseSupport getLicenseService() {
		return null;
	};

	default ISettings getSettings() {
		return null;
	};

	default IHttpClientService getHttpClientService() {
		return null;
	}

	default INpmjsPackageService getNpmjsService() {
		return null;
	}

}
