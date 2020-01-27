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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedSupport;
import org.eclipse.dash.licenses.foundation.EclipseFoundationSupport;
import org.eclipse.dash.licenses.util.Batchifier;

public class LicenseChecker {
	private ISettings settings;

	// TODO Dependency injection opportunity. Order matters.
	private ILicenseDataProvider[] dataProviders;

	public LicenseChecker(ISettings settings) {
		this.settings = settings;
		// @formatter:off
		this.dataProviders = new ILicenseDataProvider[] { 
			new EclipseFoundationSupport(settings),
			new ClearlyDefinedSupport(settings) 
		};
		// @formatter:on
	}

	/**
	 * Get the license data from the providers.
	 * 
	 * @param ids
	 * @param consumer
	 */
	public void getLicenseData(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		Set<IContentId> unresolved = new HashSet<>();
		unresolved.addAll(ids);

		for (ILicenseDataProvider provider : dataProviders) {
			Set<IContentId> resolved = new HashSet<>();
			// @formatter:off
			new Batchifier<IContentId>()
				.setBatchSize(settings.getBatchSize())
				.setConsumer(batch -> {
					provider.queryLicenseData(batch, data -> {
						consumer.accept(data);
						resolved.add(data.getId());
					});
				})
				.batchify(unresolved.stream().filter(item -> item.isValid()).iterator());
			// @formatter:on
			unresolved.removeAll(resolved);
		}

		unresolved.forEach(id -> new InvalidContentData(id));
	}
}