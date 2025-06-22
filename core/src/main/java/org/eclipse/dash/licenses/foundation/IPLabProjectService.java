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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.dash.api.EclipseApi;
import org.eclipse.dash.api.Project;
import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.ISettings;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

public class IPLabProjectService implements ILicenseDataProvider {
	@Inject
	ISettings settings;
	@Inject
	GitLabApi gitlabApi;
	@Inject
	EclipseApi eclipseApi;

	final Logger logger = LoggerFactory.getLogger(IPLabProjectService.class);

	List<Hint> hints;
	
	@Inject
	void init() throws GitLabApiException {
		var file = gitlabApi.getRepositoryFileApi().getFile(settings.getIpLabRepositoryPath(), "projects/namespace.hints", "HEAD");
		var contents = file.getDecodedContentAsString();
		hints = Arrays.stream(contents.split("\n")).map(each -> new Hint(each)).collect(Collectors.toList());
	}
	
	@Override
	public void queryLicenseData(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		if (ids.isEmpty())
			return;

		String url = settings.getLicenseCheckUrl();
		if (url.isBlank()) {
			logger.debug("Bypassing Eclipse Foundation.");
			return;
		}

		logger.info("Finding Eclipse project content data for {} items.", ids.size());

		for(IContentId id : ids) {
			findProject(id, consumer);
		}
	}

	private boolean findProject(IContentId id, Consumer<IContentData> consumer) {
		for(Hint hint: hints) {
			if (hint.matches(id)) {
				Project project = eclipseApi.getProject(hint.getProjectId());
				if (project.exists()) {
					consumer.accept(new ProjectContentData(id, project));
					return true;
				}
			}
		}
		
		return false;
	}
	
	class Hint {
		String projectId;
		List<Pattern> parts;

		public Hint(String hint) {
			var all = hint.split(",");
			projectId = all[0];
			parts = Arrays.stream(all[1].split("/")).map(each -> Pattern.compile(each.replace("*", ".*"))).collect(Collectors.toList());
		}

		public boolean matches(IContentId id) {
			return type().matcher(id.getType()).matches()
				&& source().matcher(id.getSource()).matches()
				&& namespace().matcher(id.getNamespace()).matches()
				&& name().matcher(id.getNamespace()).matches();
			// FIXME test the version
		}
		
		Pattern type() {
			return part(0);
		}
		
		Pattern source() {
			return part(1);
		}
		
		Pattern namespace() {
			return part(2);
		}
		
		Pattern name() {
			return part(3);
		}
		
		Pattern part(int index) {
			return parts.get(index);
		}

		public String getProjectId() {
			return projectId;
		}
	}
}

