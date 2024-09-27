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

import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.IProxySettings;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.util.GitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class GitLabSupport {

	final Logger logger = LoggerFactory.getLogger(GitLabSupport.class);
	private static final int MAXIMUM_REVIEWS = 100;

	@Inject
	ISettings settings;

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
					GitLabReview review = new GitLabReview(settings.getProjectId(), getRepository(), licenseData);

					Optional<GitLabIssue> oGitLabIssue = connection.findIssue(review);
					if (oGitLabIssue.isPresent()) {
						oGitLabIssue.ifPresent(issue -> {
							monitor.accept(licenseData.getId(), issue.getWebUrl());
							logger.info("A review request already exists {} .", issue.getWebUrl());
						});

						continue;
					}

					GitLabIssue created = connection.createIssue(review);
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

	String getRepository() {
		var repository = settings.getRepository();
		if (repository != null)
			return repository;

		return GitUtils.getEclipseRemote();
	}

	public void execute(Consumer<GitLabConnection> callable) {

		HttpClient.Builder builder = HttpClient.newBuilder();
		IProxySettings proxySettings = this.proxySettings.get();
		if (proxySettings != null) {
			// Configure GitLab API for the proxy server
			proxySettings.configure(builder);
		}

		try (GitLabApi gitLabApi = new GitLabApi(settings.getIpLabHostUrl(), settings.getIpLabToken(), builder)) {
			callable.accept(new GitLabConnection(gitLabApi));
		}
	}
}
