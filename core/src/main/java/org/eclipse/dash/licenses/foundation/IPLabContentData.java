/*************************************************************************
 * Copyright (c) 2025 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.foundation;

import java.util.regex.Pattern;

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseSupport.Status;

import jakarta.json.JsonObject;

public class IPLabContentData implements IContentData {

	private IContentId id;
	private JsonObject info;

	public IPLabContentData(IContentId id, JsonObject info) {
		this.id = id;
		this.info = info;
	}

	@Override
	public IContentId getId() {
		return id;
	}

	@Override
	public String getLicense() {
		return info.getString("license", "");
	}

	@Override
	public int getScore() {
		return 100;
	}

	@Override
	public String getAuthority() {
		String authority = info.getString("authority", "");
		var pattern = Pattern.compile(
				"https:\\/\\/gitlab\\.eclipse\\.org\\/eclipsefdn\\/emo-team\\/iplab\\/-\\/issues\\/(?<iid>\\d+)");
		var matcher = pattern.matcher(authority);
		if (matcher.matches()) {
			return "#" + matcher.group("iid");
		}
		return authority;
	}

	@Override
	public String getUrl() {
		return info.getString("source", "");
	}

	@Override
	public Status getStatus() {
		return "approved".equals(info.getString("status", "restricted")) ? Status.Approved : Status.Restricted;
	}
}