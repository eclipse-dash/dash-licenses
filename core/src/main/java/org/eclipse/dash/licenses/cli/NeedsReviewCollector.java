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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.LicenseSupport.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The "Needs Review" collector tracks the results that likely require some
 * review from the IP Team. The current implementation gathers results and dumps
 * output to an {@link OutputStream} when the instance is closed.
 */
public class NeedsReviewCollector implements IResultsCollector {
	final Logger logger = LoggerFactory.getLogger(NeedsReviewCollector.class);

	private List<LicenseData> needsReview = new ArrayList<>();
	private List<LicenseData> manualReview = new ArrayList<>();

	private boolean ignoreLocals = false;

	public NeedsReviewCollector() {
		this(false);
	}

	public NeedsReviewCollector(final boolean ignoreLocals) {
		this.ignoreLocals = ignoreLocals;
	}

	@Override
	public void accept(LicenseData data) {
		if (data.getStatus() != Status.Approved) {
			if (ignoreLocals && "local".equals(data.getId().getSource())) {
				manualReview.add(data);
			} else {
				needsReview.add(data);
			}
		}
	}

	@Override
	public void close() {
		if (needsReview.isEmpty()) {
			logger.info("Vetted license information was found for all content. No further investigation is required.");
		} else {
			logger.error("License information could not be automatically verified for the following content:");
			logger.error("");
			needsReview.stream().map(LicenseData::getId).map(each -> each.toString()).sorted().forEach(logger::error);
			logger.error("");
			logger.error("This content is either not correctly mapped by the system, or requires review.");
		}

		if (ignoreLocals && !manualReview.isEmpty()) {
			logger.warn("");
			logger
					.warn("License information could not be automatically verified for the following manually installed content:");
			logger.warn("");
			manualReview.stream().map(LicenseData::getId).map(each -> each.toString()).sorted().forEach(logger::warn);
			logger.warn("");
			logger.warn("This content requires a manual review.");
		}
	}

	@Override
	public int getStatus() {
		return needsReview.size();
	}
}
