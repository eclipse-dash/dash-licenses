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

import java.io.StringReader;
import java.util.Collection;
import java.util.function.Consumer;

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseSupport.Status;
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.eclipse.dash.licenses.util.JsonUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

public class IPLabService implements ILicenseDataProvider {
	@Inject
	ISettings settings;
	@Inject
	IHttpClientService httpClientService;
	@Inject
	GitLabApi gitlabApi;

	final Logger logger = LoggerFactory.getLogger(IPLabService.class);

	@Override
	public void queryLicenseData(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		if (ids.isEmpty())
			return;

		String url = settings.getLicenseCheckUrl();
		if (url.isBlank()) {
			logger.debug("Bypassing Eclipse Foundation.");
			return;
		}

		logger.info("Querying Eclipse Foundation for license data for {} items.", ids.size());

		for(IContentId id : ids) {
			if (findProject(id, consumer)) continue;
			if (findCuration(id, consumer)) continue;
			if (findMavenAlternative(id, consumer)) continue;
		}
	}

	private boolean findProject(IContentId id, Consumer<IContentData> consumer) {
		// TODO pre-load the project hints.
		// TODO we need to be able to get project licence information, see https://gitlab.eclipse.org/eclipsefdn/it/websites/projects.eclipse.org/-/issues/396
		return false;
	}
	
	private boolean findCuration(IContentId id, Consumer<IContentData> consumer) {
		var path = "curations/" + id + "/info.json";
		logger.trace("Querying IPLab for {}.", path);
		try {
			// TODO query the tree at the "name" level to try and
			// find a compatible version (i.e., one with a similar major/minor version)
			// in the event we don't find an exact match.
			
			// AFAICT this is not rate limited for the size of the files that we're reading.
			var file = gitlabApi.getRepositoryFileApi().getFile(settings.getIpLabRepositoryPath(), path, "HEAD");
			var json = file.getDecodedContentAsString();
			// TODO There is a chance that the JSON may be misformed. Deal with that.
			var info = JsonUtils.readJson(new StringReader(json));
			IPLabContentData contentData = new IPLabContentData(id, info);
			// TODO Deal with workswith and exceptions
			if (contentData.getStatus() == Status.Approved) {
				consumer.accept(contentData);
				return true;
			}
		} catch (GitLabApiException e) {
			// "Not found" (404) is a valid condition. Throw up if anything else happens.
			if (e.getHttpStatus() != 404) {
				throw new RuntimeException(e);
			}
			logger.trace("Not found {}.", id);
		}
		
		return false;
	}
	
	/**
	 * Try to find a Maven alternative for a "p2" dependency. We maintain a file in
	 * IPLab that maps an OSGi bundle ID to a corresponding Maven groupId and
	 * artifactId. If the <code>id</code> has type "p2" and the map has an entry for
	 * the "name" (bundle id), then try to find information about the item with the
	 * corresponding GAV.
	 * 
	 * <p>
	 * We should consider how we generalise this and move it further up the stack so
	 * that we can use it to query other providers.
	 */
	private boolean findMavenAlternative(IContentId id, Consumer<IContentData> consumer) {
		return false;
	}
}

