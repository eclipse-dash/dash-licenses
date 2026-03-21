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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import jakarta.inject.Provider;

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

public class LicenseToolModule {

	private final Map<Class<?>, Object> bindings = new HashMap<>();

	public LicenseToolModule(ISettings settings) {
		this(settings, null);
	}

	public LicenseToolModule(ISettings settings, IProxySettings proxySettings) {
		Provider<IProxySettings> proxyProvider = () -> proxySettings;

		HttpClientService httpClientService = new HttpClientService();
		inject(httpClientService, "settings", settings);
		inject(httpClientService, "proxySettings", proxyProvider);

		EclipseApi eclipseApi = new EclipseApi(new EclipseApi.HttpService() {
			@Override
			public int get(String url, String contentType, Consumer<InputStream> handler) {
				return httpClientService.get(url, contentType, handler);
			}
		});

		LicenseSupport licenseSupport = new LicenseSupport();
		inject(licenseSupport, "settings", settings);
		inject(licenseSupport, "httpClientService", httpClientService);
		licenseSupport.init();

		Set<ILicenseDataProvider> providers = new HashSet<>();

		EclipseFoundationSupport foundation = new EclipseFoundationSupport() {
			@Override
			public int getWeight() {
				return 100;
			}
		};
		inject(foundation, "settings", settings);
		inject(foundation, "httpClientService", httpClientService);
		providers.add(foundation);

		if (!"skip".equals(settings.getClearlyDefinedDefinitionsUrl())) {
			ClearlyDefinedSupport clearlyDefined = new ClearlyDefinedSupport();
			inject(clearlyDefined, "settings", settings);
			inject(clearlyDefined, "httpClientService", httpClientService);
			inject(clearlyDefined, "licenseService", licenseSupport);
			invokeInit(clearlyDefined);
			providers.add(clearlyDefined);
		}

		LicenseChecker licenseChecker = new LicenseChecker();
		inject(licenseChecker, "settings", settings);
		inject(licenseChecker, "licenseDataProviders", providers);

		GitLabSupport gitLabSupport = new GitLabSupport();
		inject(gitLabSupport, "settings", settings);
		inject(gitLabSupport, "proxySettings", proxyProvider);

		ProjectService projectService = new ProjectService();
		inject(projectService, "eclipseApi", eclipseApi);
		inject(projectService, "settings", settings);
		inject(projectService, "gitlab", gitLabSupport);
		invokeInit(projectService);

		bindings.put(IHttpClientService.class, httpClientService);
		bindings.put(ISettings.class, settings);
		bindings.put(IProxySettings.class, proxySettings);
		bindings.put(EclipseApi.class, eclipseApi);
		bindings.put(LicenseSupport.class, licenseSupport);
		bindings.put(LicenseChecker.class, licenseChecker);
		bindings.put(GitLabSupport.class, gitLabSupport);
		bindings.put(ProjectService.class, projectService);
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(Class<T> type) {
		return (T) bindings.get(type);
	}

	public static void inject(Object target, String fieldName, Object value) {
		Class<?> cls = target.getClass();
		while (cls != null) {
			try {
				Field field = cls.getDeclaredField(fieldName);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException e) {
				cls = cls.getSuperclass();
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Cannot inject " + fieldName, e);
			}
		}
		throw new RuntimeException("Field not found: " + fieldName);
	}

	public static void invokeInit(Object target) {
		for (String name : new String[]{"init", "bootstrap"}) {
			try {
				Method m = target.getClass().getDeclaredMethod(name);
				m.setAccessible(true);
				m.invoke(target);
				return;
			} catch (NoSuchMethodException e) {
			} catch (Exception e) {
				throw new RuntimeException("Failed to invoke " + name + "() on " + target.getClass().getName(), e);
			}
		}
	}
}
