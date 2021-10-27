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

import java.io.StringReader;

import org.eclipse.dash.licenses.cli.FlatFileReader;
import org.junit.jupiter.api.Test;

class FlatFileReaderTests {

	@Test
	void test() {
		// @formatter:off
		var contents = "# comment\n"
				+ "The following files have been resolved:\n"
				+ "none\n"
				+ "\n"
				+ "npm/npmjs/-/highlight/7.5.0";
		// @formatter:on
		assertEquals("npm/npmjs/-/highlight/7.5.0",
				new FlatFileReader(new StringReader(contents)).getContentIds().get(0).toString());
	}

}
