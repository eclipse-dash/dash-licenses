/*************************************************************************
 * Copyright (c) 2021, 2022 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.context;

import org.eclipse.dash.licenses.IProxySettings;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedSupport;
import org.eclipse.dash.licenses.foundation.EclipseFoundationSupport;

public class LicenseToolModule extends BaseLicenseToolModule {

	public LicenseToolModule(ISettings settings) {
		this(settings, null);
	}

	public LicenseToolModule(ISettings settings, IProxySettings proxySettings) {
		super(settings, proxySettings);

		bindLicenseDataProviders(new EclipseFoundationSupport() {
			@Override
			public int getWeight() {
				return 100;
			}
		});

		if (!"skip".equals(settings.getClearlyDefinedDefinitionsUrl())) {
			bindLicenseDataProviders(new ClearlyDefinedSupport());
		}
	}

}
