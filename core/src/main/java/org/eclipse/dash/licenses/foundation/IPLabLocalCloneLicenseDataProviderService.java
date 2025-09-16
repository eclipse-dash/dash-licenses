/*************************************************************************
 * Copyright (c) 2025 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.foundation;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

/**
 * Instances of the IPLabCloneService create a local shallow clone of the IPLab repository
 * and use that local clone to make various queries. 
 */
public class IPLabLocalCloneLicenseDataProviderService implements ILicenseDataProvider {
	@Inject
	IPLabLocalCloneCurationDataService iplab;
	
	final Logger logger = LoggerFactory.getLogger(IPLabLocalCloneLicenseDataProviderService.class);
	
	@Override
	public void queryLicenseData(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		if (ids.isEmpty())
			return;

		logger.info("Querying curationed data for license data for {} items.", ids.size());

		for(IContentId id : ids) {
			findContentData(id, consumer);
		}
	}

	private boolean findContentData(IContentId id, Consumer<IContentData> consumer) {
		Optional<IContentData> first = iplab.findContentData(id).findFirst();
		first.ifPresent(consumer);
		return first.isPresent();
	}
	
	@Override
	public int getWeight() {
		return 100;
	}
}

