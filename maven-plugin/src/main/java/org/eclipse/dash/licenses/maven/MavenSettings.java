/*************************************************************************
 * Copyright (c) 2020, Red Hat Inc.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.maven;

import java.net.URI;

import org.eclipse.dash.licenses.ISettings;

/**
 * Trivial implementation of the {@link ISettings} interface for validating
 * user-given parameters and passing to the license checking tool.
 */
public class MavenSettings implements ISettings {

	private final int batch;

	private final String foundationApi;

	private final String clearlyDefinedApi;

	private final String licenses;

	private final int confidence;

	private final String iplabToken;

	private String projectId;

	private String repository;

	/**
	 * Creates a valid settings instance.
	 * @param iplabToken 
	 * @param projectId 
	 * @param repo 
	 * 
	 * @throws IllegalArgumentException if the batch or confidence values are out of
	 *                                  range, or if any of the strings cannot be
	 *                                  parsed as a valid URI
	 */
	public MavenSettings(int batch, String foundationApi, String clearlyDefinedApi, String licenses, int confidence, String projectId, String iplabToken, String repo) {
		this.iplabToken = iplabToken;
		this.projectId = projectId;
		this.repository = repo;
		if (batch < 0) {
			throw new IllegalArgumentException("batch must be a positive integer");
		}
		this.batch = batch;
		// URI::create will throw IllegalArgumentException if not a valid URI
		URI.create(foundationApi);
		this.foundationApi = foundationApi;
		URI.create(clearlyDefinedApi);
		this.clearlyDefinedApi = clearlyDefinedApi;
		URI.create(licenses);
		this.licenses = licenses;
		if (confidence < 0 || confidence > 100) {
			throw new IllegalArgumentException("confidence must be in the range 0-100");
		}
		this.confidence = confidence;
	}

	@Override
	public int getBatchSize() {
		return batch;
	}

	@Override
	public String getLicenseCheckUrl() {
		return foundationApi;
	}

	@Override
	public String getClearlyDefinedDefinitionsUrl() {
		return clearlyDefinedApi;
	}

	@Override
	public String getApprovedLicensesUrl() {
		return licenses;
	}

	@Override
	public int getConfidenceThreshold() {
		return confidence;
	}
	
	@Override
	public String getProjectId() {
		return projectId;
	}	
	
	@Override
	public String getRepository() {
		if (repository != null && repository.isBlank()) return null;
		return repository;
	}
	
	@Override
	public String getIpLabToken() {
		return iplabToken;
	}
}
