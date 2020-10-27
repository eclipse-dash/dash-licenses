/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

public interface ISettings {

	int getBatchSize();

	/**
	 * The license check URL is the address of the Eclipse Foundation license data
	 * service.
	 *
	 * @return the URL (String)
	 */
	String getLicenseCheckUrl();

	String getClearlyDefinedDefinitionsUrl();

	String getApprovedLicensesUrl();

	int getConfidenceThreshold();

	/**
	 * How long do we wait for a response from a license data provider?
	 *
	 * @return the timeout in seconds (int).
	 */
	default int getTimeout() {
		return 60;
	}
}