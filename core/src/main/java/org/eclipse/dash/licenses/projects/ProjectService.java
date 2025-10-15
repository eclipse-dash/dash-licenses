/*************************************************************************
 * Copyright (c) 2025 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.projects;

import java.util.function.Consumer;

import org.eclipse.dash.api.EclipseApi;
import org.eclipse.dash.api.Project;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.review.GitLabSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

public class ProjectService {
	final Logger logger = LoggerFactory.getLogger(ProjectService.class);

	@Inject
	EclipseApi eclipseApi;
	
	@Inject
	ISettings settings;

	@Inject
	GitLabSupport gitlab;

	Project project;
	
	@Inject
	void init() {
		project = eclipseApi.getProject(settings.getProjectId());
	}
	
	public Project getProject() {
		return project;
	}

	public boolean validate(String id, Consumer<String> output) {
		var project = getProject();
		if (!project.exists()) {
			output.accept("The specified project cannot be found. You must provide a valid Eclipse project id.");
			output.accept("Specify the project as [tlp].[name] (e.g., technology.dash)");
			try {
				gitlab.execute(connection -> {
					var user = connection.getUserId();
					var account = eclipseApi.getAccount(user);
					if (account.exists() || account.isCommitter()) {
						output.accept("For example:");
						var projects = eclipseApi.getRoles(account);
						projects.stream()
								.filter(each -> each.isCommitter())
								.map(each -> each.getProject())
								.distinct()
								.sorted()
								.forEach(each -> output.accept(" - " + each));
					}
				});
			} catch (RuntimeException e) {
				logger.debug("Encountered an error while querying for user's projects", e);
			}
			return false;
		}
		return true;
	}
}
