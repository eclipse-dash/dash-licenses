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

import org.eclipse.dash.licenses.IProxySettings;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseChecker;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedSupport;
import org.eclipse.dash.licenses.extended.ExtendedContentDataService;
import org.eclipse.dash.licenses.extended.GitHubExtendedContentDataProvider;
import org.eclipse.dash.licenses.extended.IExtendedContentDataProvider;
import org.eclipse.dash.licenses.extended.MavenCentralExtendedContentDataProvider;
import org.eclipse.dash.licenses.extended.NpmjsExtendedContentDataProvider;
import org.eclipse.dash.licenses.extended.PypiExtendedContentDataProvider;
import org.eclipse.dash.licenses.foundation.EclipseFoundationSupport;
import org.eclipse.dash.licenses.http.HttpClientService;
import org.eclipse.dash.licenses.http.IHttpClientService;
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
		bind(ISettings.class).toInstance(settings);
		bind(LicenseChecker.class).toInstance(new LicenseChecker());
		bind(EclipseFoundationSupport.class).toInstance(new EclipseFoundationSupport());
		bind(ClearlyDefinedSupport.class).toInstance(new ClearlyDefinedSupport());
		bind(LicenseSupport.class).toInstance(new LicenseSupport());
		bind(GitLabSupport.class).toInstance(new GitLabSupport());
		bind(IHttpClientService.class).toInstance(new HttpClientService());
		bind(ExtendedContentDataService.class).toInstance(new ExtendedContentDataService());
		bind(IProxySettings.class).toProvider(Providers.of(proxySettings));

		Multibinder<IExtendedContentDataProvider> extendedContentDataProviders = Multibinder.newSetBinder(binder(),
				IExtendedContentDataProvider.class);
		extendedContentDataProviders.addBinding().to(NpmjsExtendedContentDataProvider.class);
		extendedContentDataProviders.addBinding().to(MavenCentralExtendedContentDataProvider.class);
		extendedContentDataProviders.addBinding().to(PypiExtendedContentDataProvider.class);
		extendedContentDataProviders.addBinding().to(GitHubExtendedContentDataProvider.class);
		// classifierBinder.addBinding().to(GithubDataProvider.class);
	}
}
