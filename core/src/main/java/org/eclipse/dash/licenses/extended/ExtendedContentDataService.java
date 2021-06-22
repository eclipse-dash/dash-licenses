/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.extended;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ISettings;

public class ExtendedContentDataService {
	@Inject
	ISettings settings;

	@Inject
	Set<IExtendedContentDataProvider> providers;

	public Stream<ExtendedContentData> findFor(IContentId contentId) {
		return providers.stream().map(provider -> provider.getExtendedContentData(contentId)).filter(Objects::nonNull);
	}
}
