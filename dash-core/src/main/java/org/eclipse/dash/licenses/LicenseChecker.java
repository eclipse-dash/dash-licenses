/*************************************************************************
 * Copyright (c) 2019, 2020 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.dash.licenses.LicenseSupport.Status;
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
	 * @return
	 */
	public Map<IContentId, LicenseData> getLicenseData(Collection<IContentId> ids) {
		Map<IContentId, LicenseData> licenseData = ids.stream().map(id -> new LicenseData(id)).collect(
				Collectors.toMap(LicenseData::getId, Function.identity(), (existing, replacement) -> existing));

		for (ILicenseDataProvider provider : dataProviders) {
			// @formatter:off
			new Batchifier<IContentId>()
				.setBatchSize(settings.getBatchSize())
				.setConsumer(batch -> {
					provider.queryLicenseData(batch, data -> {
						licenseData.get(data.getId()).addContentData(data);
					});
				})
				.batchify(ids.stream()
						.filter(IContentId::isValid)
						.filter(id -> licenseData.get(id).getStatus() != Status.Approved)
						.iterator());
			// @formatter:on
		}
		return licenseData;
	}
}