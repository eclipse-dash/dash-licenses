package org.eclipse.dash.licenses.context;

import java.util.List;
import java.util.Optional;

import org.eclipse.dash.api.EclipseApi;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.IProxySettings;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.eclipse.dash.licenses.review.GitLabSupport;

public interface LicenseToolContext {

	ISettings getSettings();

	Optional<IProxySettings> proxySettings();

	List<ILicenseDataProvider> getLicenseDataProviders();

	void bindLicenseDataProviders(ILicenseDataProvider licenseDataProvider);

	IHttpClientService getHttpClientService();

	LicenseSupport getLicenseService();

	EclipseApi getEclipseApi();

	GitLabSupport getGitlab();

}
