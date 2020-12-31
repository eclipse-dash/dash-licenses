/*************************************************************************
 * Copyright (c) 2020, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Project {
	private static Pattern projectIdPattern = Pattern.compile("[\\w\\-]+(?:\\.[\\w\\-]+){0,2}");
	private String projectId;

	private Project(String projectId) {
		this.projectId = projectId;
	}

	public static Project getProject(String projectId) {
		if (projectId == null)
			return null;
		// Rudimentary protection against injection.
		Matcher matcher = projectIdPattern.matcher(projectId);
		if (!matcher.matches())
			return null;

		return new Project(projectId);
	}

	public String getUrl() {
		return "https://projects.eclipse.org/projects/" + projectId;
	}

	public String getName() {
		/*
		 * For now, we return the id. Eventually, we'll query the EF API to get
		 * information about the project so that we can display more useful information.
		 * 
		 * FIXME Get the actual project name
		 */
		return projectId;
	}
}
