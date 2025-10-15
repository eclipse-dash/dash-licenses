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
import java.util.function.Consumer;

import org.eclipse.dash.api.EclipseApi;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.IProxySettings;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseChecker;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedSupport;
import org.eclipse.dash.licenses.foundation.EclipseFoundationSupport;
import org.eclipse.dash.licenses.http.HttpClientService;
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.eclipse.dash.licenses.projects.ProjectService;
import org.eclipse.dash.licenses.review.GitLabSupport;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Providers;

public class LicenseToolModule extends AbstractModule {

	private ISettings settings;
	private IProxySettings proxySettings;

	public LicenseToolModule(ISettings settings) {
		this(settings, null);
	}

	public LicenseToolModule(ISettings settings, IProxySettings proxySettings) {
		this.settings = settings;
		this.proxySettings = proxySettings;
	}

	@Override
	protected void configure() {
		HttpClientService httpClientService = new HttpClientService();
		bind(IHttpClientService.class).toInstance(httpClientService);
		bind(ISettings.class).toInstance(settings);
		bind(LicenseChecker.class).toInstance(new LicenseChecker());
		bind(ProjectService.class).toInstance(new ProjectService());
		bind(EclipseApi.class).toInstance(new EclipseApi(new EclipseApi.HttpService() {
			@Override
			public int get(String url, String contentType, Consumer<InputStream> handler) {
				return httpClientService.get(url, contentType, handler);
			}
		}));

		var licenseDataProviders = Multibinder.newSetBinder(binder(), ILicenseDataProvider.class);
		licenseDataProviders.addBinding().toInstance(new EclipseFoundationSupport() {
			@Override
			public int getWeight() {
				return 100;
			}
		});

		if (!"skip".equals(settings.getClearlyDefinedDefinitionsUrl())) {
			licenseDataProviders.addBinding().to(ClearlyDefinedSupport.class);
		}

		bind(LicenseSupport.class).toInstance(new LicenseSupport());
		bind(GitLabSupport.class).toInstance(new GitLabSupport());
		bind(IProxySettings.class).toProvider(Providers.of(proxySettings));
	}
}
