/*************************************************************************
 * Copyright (c) 2024 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.npm;

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseSupport.Status;

/**
 * A single unit of content answered by a query to the npm registry. The npm
 * registry exposes license information either as a top-level <code>license</code>
 * string (modern packages) or as a <code>licenses</code> array of objects
 * (legacy packages). This class represents the SPDX expression that was
 * extracted from that metadata for a given {@link IContentId}.
 *
 * @see NpmLicenseSupport
 */
public class NpmRegistryContentData implements IContentData {

	public static final String NPMJS = "npmjs";

	private final IContentId id;
	private final String license;
	private final String url;
	private Status status = Status.Restricted;

	public NpmRegistryContentData(IContentId id, String license, String url) {
		this.id = id;
		this.license = license;
		this.url = url;
	}

	@Override
	public IContentId getId() {
		return id;
	}

	@Override
	public String getLicense() {
		return license;
	}

	@Override
	public int getScore() {
		// The npm registry does not provide a confidence score the way
		// ClearlyDefined does. We report the data as either fully trusted (when the
		// declared license is approved) or not at all.
		return status == Status.Approved ? 100 : 0;
	}

	@Override
	public String getAuthority() {
		return NPMJS;
	}

	@Override
	public String getUrl() {
		return url;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public Status getStatus() {
		return status;
	}
}
