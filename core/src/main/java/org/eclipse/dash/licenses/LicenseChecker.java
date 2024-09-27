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
import java.util.stream.Stream;

import org.eclipse.dash.licenses.LicenseSupport.Status;
import org.eclipse.dash.licenses.context.LicenseToolContext;
import org.eclipse.dash.licenses.util.Batchifier;

public class LicenseChecker {

	private LicenseToolContext ctx;

	public LicenseChecker(LicenseToolContext context) {
		this.ctx = context;

	}

	private Stream<ILicenseDataProvider> getLicenseDataProviders() {
		// Compare in reverse order. We want the "heaviest" one first.
		return ctx.getLicenseDataProviders().stream().sorted((a, b) -> Integer.compare(b.getWeight(), a.getWeight()));
	}

	/**
	 * Get the license data from the providers.
	 *
	 * @param ids
	 * @param consumer
	 * @return
	 */
	public Map<IContentId, LicenseData> getLicenseData(Collection<IContentId> ids) {
		Map<IContentId, LicenseData> licenseData = ids
				.stream()
				.map(id -> new LicenseData(id))
				.collect(
						Collectors.toMap(LicenseData::getId, Function.identity(), (existing, replacement) -> existing));

		getLicenseDataProviders().forEach(provider -> {
			new Batchifier<IContentId>().setBatchSize(ctx.getSettings().getBatchSize()).setConsumer(batch -> {
				provider.queryLicenseData(batch, data -> {
					var item = licenseData.get(data.getId());
					if (item != null)
						item.addContentData(data);
				});
			})
					.batchify(ids
							.stream()
							.filter(IContentId::isValid)
							.filter(id -> licenseData.get(id).getStatus() != Status.Approved)
							.iterator());
		});

		return licenseData;
	}
}