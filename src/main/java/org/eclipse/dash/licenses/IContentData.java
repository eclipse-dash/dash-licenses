/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
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

	String getUrl();
}