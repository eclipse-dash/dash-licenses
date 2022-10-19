/*************************************************************************
 * Copyright (c) 2021, 2022 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.review;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.IProxySettings;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.extended.ExtendedContentData;
import org.eclipse.dash.licenses.extended.ExtendedContentDataService;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class GitLabSupport {

	final Logger logger = LoggerFactory.getLogger(GitLabSupport.class);
	private static final int MAXIMUM_REVIEWS = 100;

	@Inject
	ISettings settings;

	@Inject
	ExtendedContentDataService dataService;

	/** Optional HTTP proxy settings. */
	@Inject
	Provider<IProxySettings> proxySettings;

	public void createReviews(List<LicenseData> needsReview, BiConsumer<IContentId, String> monitor) {
		execute(connection -> {
			var count = 0;
			for (LicenseData licenseData : needsReview) {
				if (count >= MAXIMUM_REVIEWS)
					break;
				count++;

				if (!licenseData.getId().isValid()) {
					logger.info("I don't know what to do with {}.", licenseData.getId().toString());
					continue;
				}

				logger.info("A review is required for {}.", licenseData.getId().toString());

				Stream<ExtendedContentData> extendedData = dataService.findFor(licenseData.getId());

				/*
				 * Ideally, we need a way to "create if does not already exist" feature in the
				 * GitLab API. But since we don't have that, we'll leverage the expectation that
				 * concurrent requests to review the same content will be relatively rare (there
				 * is some risk that between asking if we have an existing issue for a review
				 * for a particular bit of content and creating a new one, that somebody else
				 * might be doing the same). Our expectation is that the potential additional
				 * churn on the backend should require significantly less effort than that
				 * required to prevent rare duplication.
				 */
				try {
					GitLabReview review = new GitLabReview(settings.getProjectId(), licenseData, extendedData);

					Issue existing = connection.findIssue(review);
					if (existing != null) {
						monitor.accept(licenseData.getId(), existing.getWebUrl());
						logger.info("A review request already exists {} .", existing.getWebUrl());
						continue;
					}

					Issue created = connection.createIssue(review);
					if (created == null) {
						logger.error("An error occurred while attempting to create a review request. Aborting.");
						// TODO If we break creating a review, then don't try to create any more.
						break;
					}

					monitor.accept(licenseData.getId(), created.getWebUrl());
					logger.info("A review request was created {} .", created.getWebUrl());
				} catch (GitLabApiException e) {
					throw new RuntimeException(e);
				}

			}

			if (count < needsReview.size()) {
				logger.info("More content needs to be reviewed.");
				logger.info("For now, however, this experimental feature only submits the first {}.\n", count);
			}
		});
	}

	void execute(Consumer<GitLabConnection> callable) {
		Map<String, Object> clientConfig = null;
		IProxySettings proxySettings = this.proxySettings.get();
		if (proxySettings != null) {
			// Configure GitLab API for the proxy server
			clientConfig = Maps.newHashMap();
			proxySettings.configureJerseyClient(clientConfig);
		}

		try (GitLabApi gitLabApi = new GitLabApi(settings.getIpLabHostUrl(), settings.getIpLabToken(), clientConfig)) {
			callable.accept(new GitLabConnection(gitLabApi, settings.getIpLabRepositoryPath()));
		}
	}
}
