package org.eclipse.dash.licenses.tests.util;

import java.io.StringReader;

import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseChecker;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.context.IContext;
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.eclipse.dash.licenses.review.GitLabSupport;

public class TestContext implements IContext {

	private ISettings settings = new ISettings() {

		@Override
		public int getBatchSize() {
			// TODO Auto-generated method stub
			return 10;
		}

		@Override
		public String getLicenseCheckUrl() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getClearlyDefinedDefinitionsUrl() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getApprovedLicensesUrl() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getConfidenceThreshold() {
			// TODO Auto-generated method stub
			return 0;
		}

	};

	@Override
	public LicenseChecker getLicenseCheckerService() {
		return new LicenseChecker(this);
	}

	@Override
	public GitLabSupport getGitLabService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILicenseDataProvider getIPZillaService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILicenseDataProvider getClearlyDefinedService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LicenseSupport getLicenseService() {
		return LicenseSupport.getLicenseSupport(new StringReader("{}"));
	}

	@Override
	public ISettings getSettings() {
		return settings;
	}

	@Override
	public IHttpClientService getHttpClientService() {
		// TODO Auto-generated method stub
		return null;
	}

}
