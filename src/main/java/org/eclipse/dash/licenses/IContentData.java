package org.eclipse.dash.licenses;

import org.eclipse.dash.licenses.LicenseSupport.Status;

public interface IContentData {

	IContentId getId();

	String getLicense();

	int getScore();

	default Status getStatus() {
		return Status.Restricted;
	}

	String getAuthority();

}