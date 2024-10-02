/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.cli.CommandLineSettings;
import org.junit.jupiter.api.Test;

class CommandLineSettingsTest {

	@Test
	void testCustomBatchSize() {
		ISettings settings = CommandLineSettings.getSettings(new String[] { "-batch", "42" });
		assertEquals(42, settings.getBatchSize());
	}

	@Test
	void testDefaultBatchSize() {
		ISettings settings = CommandLineSettings.getSettings(new String[] {});
		assertEquals(ISettings.DEFAULT_BATCH, settings.getBatchSize());
	}

	@Test
	void testInvalidBatchSize() {
		CommandLineSettings settings = CommandLineSettings.getSettings(new String[] { "-batch", "xx" });
		assertFalse(settings.isValid());
	}
	
	@Test
	void testCustomConfidence() {
		ISettings settings = CommandLineSettings.getSettings(new String[] { "-confidence", "42" });
		assertEquals(42, settings.getConfidenceThreshold());
	}

	@Test
	void testDefaultConfidence() {
		ISettings settings = CommandLineSettings.getSettings(new String[] {});
		assertEquals(ISettings.DEFAULT_THRESHOLD, settings.getConfidenceThreshold());
	}

}
