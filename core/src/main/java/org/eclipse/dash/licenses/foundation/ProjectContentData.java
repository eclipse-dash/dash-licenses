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

import org.eclipse.dash.api.Project;
import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseSupport.Status;

public class ProjectContentData implements IContentData {

	private IContentId id;
	private Project project;

	public ProjectContentData(IContentId id, Project project) {
		this.id = id;
		this.project = project;
	}

	@Override
	public IContentId getId() {
		return id;
	}

	@Override
	public String getLicense() {
		// FIXME https://gitlab.eclipse.org/eclipsefdn/it/websites/projects.eclipse.org/-/issues/396
		return "EPL-2.0";
	}

	@Override
	public int getScore() {
		return 100;
	}

	@Override
	public String getAuthority() {
		return project.getId();
	}

	@Override
	public String getUrl() {
		return project.getUrl();
	}

	@Override
	public Status getStatus() {
		return Status.Approved;
	}
}
