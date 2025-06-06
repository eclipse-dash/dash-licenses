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

import java.io.File;

public interface ISettings {

	/**
	 * Default timeout in seconds. We're patient.
	 */
	public static final int DEFAULT_TIMEOUT = 60 * 2;
	public static final String DEFAULT_GITLAB_URL = "https://gitlab.eclipse.org";
	public static final String DEFAULT_IPLAB_PATH = "eclipsefdn/emo-team/iplab";
	public static final String DEFAULT_APPROVED_LICENSES_URL = "https://www.eclipse.org/legal/licenses/licenses.json";
	public static final String DEFAULT_CLEARLYDEFINED_URL = "https://api.clearlydefined.io/definitions";
	public static final String DEFAULT_IPZILLA_URL = "https://www.eclipse.org/projects/services/license_check.php";
	public static final int DEFAULT_THRESHOLD = 60;
	public static final int DEFAULT_BATCH = 500;

	default int getBatchSize() {
		String value = System.getProperty("org.eclipse.dash.batch");
		if (value == null)
			return DEFAULT_BATCH;

		int threshold = Integer.valueOf(value);
		if (threshold < 1)
			return 1;

		return threshold;
	};

	/**
	 * The license check URL is the address of the Eclipse Foundation license data
	 * service.
	 *
	 * @return the URL (String)
	 */
	default String getLicenseCheckUrl() {
		return System.getProperty("org.eclipse.dash.ipzilla", DEFAULT_IPZILLA_URL);
	};

	default String getClearlyDefinedDefinitionsUrl() {
		return System.getProperty("org.eclipse.dash.clearlydefined", DEFAULT_CLEARLYDEFINED_URL);
	};

	default String getApprovedLicensesUrl() {
		return System.getProperty("org.eclipse.dash.licenses", DEFAULT_APPROVED_LICENSES_URL);
	};

	/**
	 * The confidence threshold is used to specify the minimum licence score to
	 * approve components based on licence data received from ClearlyDefined.
	 * 
	 * <p>
	 * The ClearlyDefined license score is a metric designed to quantify the clarity
	 * of a software component's licensing information. It serves as an indicator of
	 * how easily consumers can understand and comply with the software's licensing
	 * terms. It is calculated by evaluating factors like the presence of clear
	 * top-level license declarations, per-file license and copyright notices,
	 * consistency between different license information sources, inclusion of
	 * complete license texts, and the use of standard license identifiers such as
	 * SPDX. A higher license score signifies that the software's licensing
	 * information is more comprehensive, consistent, and readily accessible,
	 * thereby simplifying the process of understanding and adhering to the
	 * licensing requirements.
	 */
	default int getConfidenceThreshold() {
		String value = System.getProperty("org.eclipse.dash.threshold");
		if (value == null)
			return DEFAULT_THRESHOLD;

		int threshold = Integer.valueOf(value);
		if (threshold < 0)
			return 0;
		if (threshold > 100)
			return 100;

		return threshold;
	};

	default String getProjectId() {
		return System.getProperty("org.eclipse.dash.project");
	}

	/**
	 * How long do we wait for a response from a license data provider?
	 *
	 * @return the timeout in seconds (int).
	 */
	default int getTimeout() {
		var value = System.getProperty("org.eclipse.dash.timeout");
		if (value != null) {
			try {
				var timeout = Integer.parseInt(value);
				if (timeout > 0)
					return timeout;
			} catch (NumberFormatException e) {
				return DEFAULT_TIMEOUT;
			}
		}
		return DEFAULT_TIMEOUT;
	}

	/**
	 * The GitLab authentication token that we need to connect to GitLab to create
	 * requests for review.
	 * 
	 * @return the token or <code>null</code> if no value is available.
	 */
	default String getIpLabToken() {
		String token = System.getProperty("org.eclipse.dash.token");
		if (token == null) {
			return System.getenv("DASH_TOKEN");
		}
		return token;
	}

	/**
	 * The URL of the GitLab host of the "IP Lab" repository.
	 * 
	 * @return A valid URL.
	 */
	default String getIpLabHostUrl() {
		return System.getProperty("org.eclipse.dash.iplab-host", DEFAULT_GITLAB_URL);
	}

	/**
	 * The path (relative to the GitLab host) of the path of the "IP Lab"
	 * repository.
	 * 
	 * @return A valid path.
	 */
	default String getIpLabRepositoryPath() {
		return System.getProperty("org.eclipse.dash.iplab-path", DEFAULT_IPLAB_PATH);
	}

	default String getSummaryFilePath() {
		return System.getProperty("org.eclipse.dash.summary");
	};

	default File getSummaryFile() {
		return new File(getSummaryFilePath());
	}
	
	default String getRepository() {
		return System.getProperty("org.eclipse.dash.repo");
	}
}