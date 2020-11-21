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
import java.util.HashSet;
import java.util.List;

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.LicenseSupport.Status;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedContentData;

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
		output.println(String.format("* [ ] %s", licenseData.getId()));
		licenseData.contentData().forEach(data -> describeItem(data));
	}

	/**
	 * THis method writes potentially helpful information to make the intellectual
	 * review process as easy as possible to the output writer.
	 * 
	 * @param data
	 */
	private void describeItem(IContentData data) {
		// FIXME This is clunky

		String authority = data.getAuthority();
		if (data.getUrl() != null)
			authority = String.format("[%s](%s)", authority, data.getUrl());
		output.println(String.format("  - %s %s (%d)", authority, data.getLicense(), data.getScore()));
		switch (data.getAuthority()) {
		case ClearlyDefinedContentData.CLEARLYDEFINED:
			((ClearlyDefinedContentData) data).discoveredLicenses()
					.forEach(license -> output.println("    - " + license));
		};
		output.println(String.format("  - [Search IPZilla](%s)", getIPZillaSearchUrl(data)));
		IContentId id = data.getId();
		if ("maven".equals(id.getType()) && "mavencentral".equals(id.getSource())) {
			output.println(String.format("  - [Maven Central](https://search.maven.org/artifact/%s/%s/%s/jar)",
					id.getNamespace(), id.getName(), id.getVersion()));
		}
		if ("npm".equals(id.getType()) && "npmjs".equals(id.getSource())) {
			var builder = new StringBuilder();
			if (!"-".equals(id.getNamespace())) {
				builder.append(id.getNamespace());
				builder.append('/');
			}
			builder.append(id.getName());
			output.println(String.format("  - [npmjs.com](https://www.npmjs.com/package/%s/v/%s)", builder.toString(),
					id.getVersion()));
		}
	}

	private String getIPZillaSearchUrl(IContentData data) {
		var terms = new HashSet<String>();

		String namespace = data.getId().getNamespace();
		String name = data.getId().getName();

		// Assemble terms from the content data that might result
		// in an interesting search.
		terms.add(namespace);
		terms.add(name);

		// Add the last segment from the name to the search terms.
		// We arbitrarily decide that terms that are "short" aren't interesting
		// and skip them. The logic being that shorter words are more likely to
		// be common, and common words will clutter up our search.
		String lastSegment = name.substring(1 + name.lastIndexOf('.'));
		if (lastSegment.length() >= 8)
			terms.add(lastSegment);

		var builder = new StringBuilder();
		builder.append("https://dev.eclipse.org/ipzilla/buglist.cgi");
		builder.append("?short_desc_type=anywords");
		builder.append("&short_desc=");
		builder.append(String.join("+", terms));
		builder.append("&long_desc_type=substring");

		return builder.toString();
	}

	@Override
	public int getStatus() {
		return needsReview.size();
	}
}