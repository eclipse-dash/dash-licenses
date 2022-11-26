/*************************************************************************
 * Copyright (c) 2020,2022 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.review;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.LicenseSupport.Status;
import org.eclipse.dash.licenses.cli.IResultsCollector;

/**
 * The "Create Review Request" collector tracks the results that likely require
 * some review from the IP Team. The gathered information is output to an
 * {@link OutputStream} in Markdown format, suitable for use as a description in
 * a review request.
 */
public class CreateReviewRequestCollector implements IResultsCollector {

	private GitLabSupport gitLab;
	private List<LicenseData> needsReview = new ArrayList<>();
	private BiConsumer<IContentId, String> monitor;

	public CreateReviewRequestCollector(GitLabSupport gitLab, BiConsumer<IContentId, String> monitor) {
		this.gitLab = gitLab;
		this.monitor = monitor;
	}

	@Override
	public void accept(LicenseData data) {
		if (data.getStatus() != Status.Approved && !"local".equals(data.getId().getSource())) {
			needsReview.add(data);
		}
	}

	@Override
	public void close() {
		if (!needsReview.isEmpty()) {
			gitLab.createReviews(needsReview, monitor);
		}
	}

	@Override
	public int getStatus() {
		return needsReview.size();
	}
}