/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.tests.util.TestContext;
import org.junit.jupiter.api.Test;

class LicenseCheckerTests {

	@Test
	void testApprovedLicense() {
		Map<IContentId, LicenseData> licenseData = new TestContext().getLicenseCheckerService()
				.getLicenseData(Collections.singleton(ContentId.getContentId("npm/npmjs/@yarnpkg/lockfile/1.1.0")));

		for (LicenseData data : licenseData.values()) {
			assertEquals(LicenseSupport.Status.Restricted, data.getStatus());
		}
	}
}
