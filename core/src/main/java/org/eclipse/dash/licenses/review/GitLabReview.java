/*************************************************************************
 * Copyright (c) 2020,2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.review;

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedContentData;

public class GitLabReview {
	private String projectId;
	private LicenseData licenseData;

	public GitLabReview(String projectId, LicenseData licenseData) {
		this.projectId = projectId;
		this.licenseData = licenseData;
	}

	public String getTitle() {
		return getContentId().toString();
	}

	public String getLabels() {
		return "Review Needed";
	}

	public String getDescription() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%s\n\n", licenseData.getId()));

		if (projectId != null) {
			builder
					.append(String
							.format("Project: [%s](https://projects.eclipse.org/projects/%s)\n", projectId, projectId));
		}
		licenseData.contentData().forEach(data -> describeItem(data, builder));

		return builder.toString();
	}

	/**
	 * THis method writes potentially helpful information to make the intellectual
	 * review process as easy as possible to the output writer.
	 * 
	 * @param data
	 */
	private void describeItem(IContentData data, StringBuilder output) {
		// FIXME This is clunky

		output.append("\n");
		String authority = data.getAuthority();
		if (data.getUrl() != null)
			authority = String.format("[%s](%s)", authority, data.getUrl());
		output.append(String.format("%s\n", authority));
		output.append(String.format("  - Declared: %s (%d)\n", data.getLicense(), data.getScore()));
		switch (data.getAuthority()) {
		case ClearlyDefinedContentData.CLEARLYDEFINED:
			((ClearlyDefinedContentData) data)
					.discoveredLicenses()
					.forEach(license -> output.append("  - Discovered: " + license).append('\n'));
		};
	}

	private IContentId getContentId() {
		return licenseData.getId();
	}
}
