/*************************************************************************
 * Copyright (c) 2021,2022 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.extended;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ExtendedContentData {
	public static final String ID = "ID";
	public static final String LICENSE = "License";
	public static final String REPOSITORY = "Repository";
	public static final String DISTRIBUTION = "Distribution";
	public static final String SOURCE = "Source";

	private String title;
	private String url;
	private List<ExtendedContentDataItem> items = new ArrayList<>();

	public ExtendedContentData(String title, String url) {
		this.title = title;
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public Stream<ExtendedContentDataItem> getItems() {
		return items.stream();
	}

	public void addItem(String key, String value) {
		if (value == null)
			return;
		items.add(new ExtendedContentDataItem(key, value));
	}

	public void addLink(String key, String value) {
		if (value == null)
			return;
		items.add(new ExtendedContentDataLink(key, value));
	}

	public String get(String key) {
		return getItems()
				.filter(each -> key.equals(each.getLabel()))
				.map(each -> each.getValue())
				.findAny()
				.orElse(null);
	}

	public String getId() {
		return get(ID);
	}

	public String getSourceUrl() {
		return get(SOURCE);
	}

	public String getRepository() {
		return get(REPOSITORY);
	}

	public String getDistribution() {
		return get(DISTRIBUTION);
	}

	public String getLicense() {
		return get(LICENSE);
	}
}
