/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.review;

import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedSupport;
import org.gitlab4j.api.Constants.IssueState;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.IssuesApi;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.IssueFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitLabConnection {
	final Logger logger = LoggerFactory.getLogger(ClearlyDefinedSupport.class);
	private GitLabApi gitLabApi;
	private String path;

	public GitLabConnection(GitLabApi gitLabApi, String path) {
		this.gitLabApi = gitLabApi;
		this.path = path;
	}

	public Issue findIssue(GitLabReview review) throws GitLabApiException {
		return rateLimit(() -> {

			String title = review.getTitle();

			logger.debug("Querying GitLab for {}", title);

			IssueFilter filter = new IssueFilter().withState(IssueState.OPENED);
			return getIssuesApi()
					.getIssuesStream(path, filter)
					.filter(issue -> issue.getTitle().equals(title))
					.findAny()
					.orElse(null);
		});
	}

	public Issue createIssue(GitLabReview review) throws GitLabApiException {
		return rateLimit(() -> {
			logger.debug("GitLab creating an issue for {}", review.getTitle());

			return getIssuesApi()
					.createIssue(path, review.getTitle(), review.getDescription(), false, null, null,
							review.getLabels(), null, null, null, null);
		});
	}

	interface Supplier<R> {
		R get() throws GitLabApiException;
	}

	/**
	 * This method deals with rate limits while invoking a function. The function is
	 * assumed to do something with the GitLab API that may result in a
	 * {@link GitLabApiException}. When we do hit that exception, and if the cause
	 * was a HTTP 429 return code ("Too many requests"), we wait for some period of
	 * time and then retry the function. If the exception is caused by something
	 * else, we just rethrow it.
	 * <p>
	 * We wait for 30 seconds by default, but this can be configured via system
	 * property. When the wait is set to 0 seconds, we just rethrow the exception.
	 * <p>
	 * Note that raw API actually answers back <code>x-ratelimit</code> response
	 * headers, but I don't see any way to surface them via the library in order to
	 * more intelligently decide how to wait.
	 * 
	 * @throws GitLabApiException
	 */
	private <R> R rateLimit(Supplier<R> f) throws GitLabApiException {
		try {
			return f.get();
		} catch (GitLabApiException e) {
			if (e.getHttpStatus() == 429) {
				try {
					long seconds = getPauseSeconds();

					if (seconds == 0)
						throw e;

					logger.info("GitLab API rate limit reached. Will retry in {} seconds.", seconds);
					Thread.sleep(seconds * 1000);
				} catch (InterruptedException interruped) {
					// logger.error("Sleep interrupted.", e);
				}
				return rateLimit(f);
			}
			throw e;
		}
	}

	/**
	 * This method answers the number of seconds to wait in the event that we need
	 * to pause and retry an API call (i.e., we receive a 429 from the API call).
	 * 
	 * @return
	 */
	private long getPauseSeconds() {
		// I have centralised access to all other properties in the ISettings type.
		// It's not clear to me whether or not that is a good thing.
		var value = System.getProperty("org.eclipse.dash.iplab.retry");
		if (value == null)
			return 30;
		try {
			return Math.max(0L, Long.valueOf(value));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public String getUserId() {
		// I'm pretty sure that this API isn't rate limited.
		try {
			return gitLabApi.getUserApi().getCurrentUser().getUsername();
		} catch (GitLabApiException e) {
			throw new RuntimeException(e);
		}
	}

	private IssuesApi getIssuesApi() {
		return gitLabApi.getIssuesApi();
	}
}
