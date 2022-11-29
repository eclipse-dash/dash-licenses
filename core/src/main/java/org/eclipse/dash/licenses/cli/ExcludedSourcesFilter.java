/*************************************************************************
 * Copyright (c) 2022 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.util.Arrays;
import java.util.function.Function;

import org.eclipse.dash.licenses.IContentId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcludedSourcesFilter {
	// FIXME Generalise
	final Logger logger = LoggerFactory.getLogger(ExcludedSourcesFilter.class);

	private Function<IContentId, Boolean> excludedSources;

	public ExcludedSourcesFilter(String excludedSources) {
		this.excludedSources = prepare(excludedSources);
	}

	private Function<IContentId, Boolean> prepare(String value) {
		if (value == null)
			return data -> false;

		final var values = Arrays.asList(value.split(","));
		return id -> values.stream().anyMatch(each -> each.equals(id.getSource()));
	}

	public boolean keep(IContentId data) {
		boolean accept = !excludedSources.apply(data);

		if (!accept)
			logger.debug("Excluding {} from review.", data.toString());

		return accept;
	}
}
