package org.eclipse.dash.bom;

import org.eclipse.dash.bom.LicenseSupport.Status;

public interface IContentData {

	IContentId getId();

	String getLicense();

	int getScore();

	default Status getStatus() {
		return Status.Restricted;
	}

	String getAuthority();

}