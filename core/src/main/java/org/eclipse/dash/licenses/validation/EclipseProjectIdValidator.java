/*************************************************************************
 * Copyright (c) 2023 The Eclipse Foundation
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.validation;

import java.util.function.Consumer;

import javax.inject.Inject;

import org.eclipse.dash.api.EclipseApi;
import org.eclipse.dash.licenses.review.GitLabSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EclipseProjectIdValidator {
	final Logger logger = LoggerFactory.getLogger(EclipseProjectIdValidator.class);

	@Inject
	EclipseApi eclipseApi;

	@Inject
	GitLabSupport gitlab;

	public boolean validate(String id, Consumer<String> output) {
		var project = eclipseApi.getProject(id);
		if (!project.exists()) {
			output.accept("The specified project cannot be found. You must provide a valid Eclipse project id.");
			output.accept("Specify the project as [tlp].[name] (e.g., technology.dash)");
			try {
				gitlab.execute(connection -> {
					var user = connection.getUserId();
					var account = eclipseApi.getAccount(user);
					if (account.exists() || account.isCommitter()) {
						output.accept("For example:");
						var projects = eclipseApi.getProjects(account);
						projects
								.stream()
								.flatMap(each -> each.getRoles().stream())
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
