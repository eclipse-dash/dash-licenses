/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.dash.licenses.LicenseSupport.Status;

public class LicenseData {

	private IContentId id;
	private List<IContentData> contentData = new ArrayList<>();

	public LicenseData(IContentId id) {
		this.id = id;
	}

	public IContentId getId() {
		return id;
	}

	public void addContentData(IContentData data) {
		contentData.add(data);
	}

	public String getLicense() {
		return withDefaultContentData(IContentData::getLicense, null);
	}

	public Status getStatus() {
		return withDefaultContentData(IContentData::getStatus, Status.Restricted);
	}

	public String getAuthority() {
		return withDefaultContentData(IContentData::getAuthority, null);
	}

	public String getUrl() {
		return withDefaultContentData(IContentData::getUrl, null);
	}

	public String getSourceUrl() {
		return withDefaultContentData(IContentData::getSourceUrl, null);
	}

	/**
	 * Answer the result of executing the function with the default IContentData
	 * instance. We assume that the license data sources were called in priority
	 * order and that the first one that was found is the default (i.e., the first
	 * one in the list). If no data has been found, then answer the default value.
	 */
	private <R> R withDefaultContentData(Function<IContentData, R> function, R defaultValue) {
		if (contentData.isEmpty())
			return defaultValue;
		return function.apply(contentData.get(0));
	}

	public Stream<IContentData> contentData() {
		return contentData.stream();
	}
}
