/*************************************************************************
 * Copyright (c) 2021, 2022 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.context;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.dash.api.EclipseApi;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.IProxySettings;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseChecker;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.http.HttpClientService;
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.eclipse.dash.licenses.review.GitLabSupport;

public class BaseLicenseToolModule implements LicenseToolContext {

	private ISettings settings;
	private Optional<IProxySettings> proxySettings;

	private IHttpClientService httpClientService;
	private LicenseChecker licenseChecker;
	private EclipseApi eclipseApi;
	private LicenseSupport licenseSupport;
	private GitLabSupport gitLabSupport;
	private List<ILicenseDataProvider> licenseDataProviders = new ArrayList<>();

	public BaseLicenseToolModule(ISettings settings) {
		this(settings, null);
	}

	public BaseLicenseToolModule(ISettings settings, IProxySettings proxySettings) {
		this.settings = settings;
		this.proxySettings = Optional.ofNullable(proxySettings);
		this.httpClientService = newHttpClient();

		this.licenseChecker = new LicenseChecker(this);
		this.licenseSupport = new LicenseSupport(this);

		this.eclipseApi = new EclipseApi(new EclipseApi.HttpService() {
			@Override
			public int get(String url, String contentType, Consumer<InputStream> handler) {
				return httpClientService.get(url, contentType, handler);
			}
		});
		this.gitLabSupport = new GitLabSupport(this);
	}

	protected IHttpClientService newHttpClient() {
		return new HttpClientService(this);

	}

	@Override
	public ISettings getSettings() {
		return settings;
	}

	@Override
	public Optional<IProxySettings> proxySettings() {
		return proxySettings;
	}

	@Override
	public List<ILicenseDataProvider> getLicenseDataProviders() {
		return licenseDataProviders;
	}

	@Override
	public void bindLicenseDataProviders(ILicenseDataProvider licenseDataProvider) {
		licenseDataProvider.init(this);
		licenseDataProviders.add(licenseDataProvider);
	}

	@Override
	public IHttpClientService getHttpClientService() {
		return httpClientService;
	}

	@Override
	public LicenseSupport getLicenseService() {
		return licenseSupport;
	}

	@Override
	public EclipseApi getEclipseApi() {
		return eclipseApi;
	}

	@Override
	public GitLabSupport getGitlab() {
		return gitLabSupport;
	}

	public LicenseChecker getLicenseChecker() {
		return licenseChecker;
	}

	@SuppressWarnings("unchecked")
	public <T extends ILicenseDataProvider> T getLicenseDataProviderOfType(Class<T> ldpClass) {
		return (T) licenseDataProviders.stream().filter(t -> {
			return ldpClass.isInstance(t);
		}).findFirst().orElse(null);
	}
}
