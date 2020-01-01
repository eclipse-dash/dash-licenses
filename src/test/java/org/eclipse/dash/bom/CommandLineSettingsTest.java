/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.bom;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.dash.licenses.CommandLineSettings;
import org.eclipse.dash.licenses.ISettings;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CommandLineSettingsTest {

	@Test
	void testCustomBatchSize() {
		ISettings settings = CommandLineSettings.getSettings(new String[] {"-batch", "42"});
		assertEquals(42, settings.getBatchSize());
	}

	@Test
	void testDefaultBatchSize() {
		ISettings settings = CommandLineSettings.getSettings(new String[] {});
		assertEquals(1000, settings.getBatchSize());
	}

	@Disabled("This doesn't work yet")
	@Test
	void testInvalidBatchSize() {
		ISettings settings = CommandLineSettings.getSettings(new String[] {"-batch", "xx"});
		assertNull(settings.getBatchSize());
	}
	
	@Test
	void testCustomLicenseCheckUrl() {
		String url = "http://localhost/license.php";
		ISettings settings = CommandLineSettings.getSettings(new String[] {"-ef", url});
		assertEquals(url, settings.getLicenseCheckUrl());
	}

	@Test
	void testCustomClearlyDefinedDefinitionsUrl() {
		String url = "http://localhost/license.php";
		ISettings settings = CommandLineSettings.getSettings(new String[] {"-cd", url});
		assertEquals(url, settings.getClearlyDefinedDefinitionsUrl());
	}

	@Test
	void testCustomApprovedLicensesUrl() {
		String url = "http://localhost/license.php";
		ISettings settings = CommandLineSettings.getSettings(new String[] {"-wl", url});
		assertEquals(url, settings.getApprovedLicensesUrl());
	}

}
