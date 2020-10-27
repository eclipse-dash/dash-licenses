/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.foundation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.LicenseSupport.Status;

import jakarta.json.JsonObject;

public class FoundationData implements IContentData {

	private static final Pattern COMPILE = Pattern.compile("CQ(?<id>[0-9]+)");

	/**
	 * This field holds data that presents a single unit of content answered by a
	 * query to the Eclipse Foundation. The value is in JSON format as in this
	 * example:
	 * 
	 * <pre>
	 * {
	 *       "authority": "CQ7766", 
	 *       "confidence": "100", 
	 *       "definitionUrl": null, 
	 *       "id": "maven/mavencentral/com.google.guava/guava/15.0", 
	 *       "license": "Apache-2.0", 
	 *       "sourceUrl": null, 
	 *       "status": "approved"
	 *   }
	 * </pre>
	 */
	private JsonObject data;

	public FoundationData(JsonObject data) {
		this.data = data;
	}

	@Override
	public IContentId getId() {
		return ContentId.getContentId(data.getString("id"));
	}

	@Override
	public String getLicense() {
		return data.getString("license");
	}

	@Override
	public int getScore() {
		return data.getInt("confidence");
	}

	@Override
	public Status getStatus() {
		if ("approved".equals(data.getString("status")))
			return LicenseSupport.Status.Approved;
		return LicenseSupport.Status.Restricted;
	}

	@Override
	public String getAuthority() {
		return data.getString("authority");
	}

	@Override
	public String getUrl() {
		String cq = getCqId();
		if (cq == null)
			return null;

		return "https://dev.eclipse.org/ipzilla/show_bug.cgi?id=" + cq;
	}

	private String getCqId() {
		Matcher matcher = COMPILE.matcher(getAuthority());
		if (matcher.matches()) {
			return matcher.group("id");
		}
		return null;
	}
}
