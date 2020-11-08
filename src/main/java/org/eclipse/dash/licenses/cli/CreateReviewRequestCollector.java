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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.LicenseSupport.Status;

/**
 * The "Create Review Request" collector tracks the results that likely require
 * some review from the IP Team. The gathered information is output to an
 * {@link OutputStream} in Markdown format, suitable for use as a description in
 * a review request.
 */
public class CreateReviewRequestCollector implements IResultsCollector {

	private PrintWriter output;
	private List<LicenseData> needsReview = new ArrayList<>();
	private Project project;

	public CreateReviewRequestCollector(Project project, OutputStream out) {
		this.project = project;
		output = new PrintWriter(out);
	}

	@Override
	public void accept(LicenseData data) {
		if (data.getStatus() != Status.Approved) {
			needsReview.add(data);
		}
	}

	@Override
	public void close() {
		if (needsReview.isEmpty()) {
			output.println(
					"Vetted license information was found for all content. No further investigation is required.");
		} else {
			if (project != null) {
				output.println(String.format("Project: [%s](%s)", project.getName(), project.getUrl()));
				output.println();
			}
			output.println("The following content requires review:");
			output.println();
			needsReview.stream().sorted((a, b) -> a.getId().compareTo(b.getId())).forEach(each -> describe(each));
			output.println();
		}
		output.flush();
	}

	private void describe(LicenseData licenseData) {
		output.println(String.format("* %s", licenseData.getId()));
		licenseData.contentData().forEach(data -> output
				.println(String.format("  - [%s](%s) %s", data.getAuthority(), data.getUrl(), data.getLicense())));
	}

	@Override
	public int getStatus() {
		return needsReview.size();
	}
}