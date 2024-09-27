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

public class GitLabUser {

	private String username;

	public GitLabUser() {
	}

	public GitLabUser withUsername(String username) {
		this.username = username;
		return this;
	}

	public String getUsername() {
		return username;
	}

}
