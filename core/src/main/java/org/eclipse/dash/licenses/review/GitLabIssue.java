/*************************************************************************
 * Copyright (c) 2020,2024 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.review;

public class GitLabIssue {

	private String title;
	private String webUrl;

	public GitLabIssue() {

	}

	public Object getTitle() {
		return title;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public GitLabIssue withTitle(String title) {
		this.title = title;
		return this;
	}

	public GitLabIssue withWebUrl(String webUrl) {
		this.webUrl = webUrl;
		return this;
	}
}
