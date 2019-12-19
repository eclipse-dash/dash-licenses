package org.eclipse.dash.bom;

import static org.junit.jupiter.api.Assertions.*;

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
