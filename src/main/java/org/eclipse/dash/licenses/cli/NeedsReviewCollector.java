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
import java.util.Objects;

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.LicenseSupport.Status;

/**
 * The "Needs Review" collector tracks the results that likely require some
 * review from the IP Team. The current implementation gathers results and dumps
 * output to an {@link OutputStream} when the instance is closed.
 */
public class NeedsReviewCollector implements IResultsCollector {

	private PrintWriter output;
	private List<IContentData> needsReview = new ArrayList<>();

	public NeedsReviewCollector(OutputStream out) {
		output = new PrintWriter(out);
	}

	@Override
	public void accept(IContentData data) {
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
			output.println("License information could not be automatically verified for the following content:");
			output.println();
			needsReview.stream().map(IContentData::getUrl).filter(Objects::nonNull)
					.forEach(each -> output.println(each));
			output.println();
			output.println("Please create contribution questionnaires for this content.");
		}
		output.flush();
	}

	@Override
	public int getStatus() {
		return needsReview.size();
	}
}
