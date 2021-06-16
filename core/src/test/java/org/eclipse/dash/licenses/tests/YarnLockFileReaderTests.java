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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.dash.licenses.cli.YarnLockFileReader;
import org.junit.jupiter.api.Test;

class YarnLockFileReaderTests {

	@Test
	void test() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("/yarn.lock")) {
			var ids = new YarnLockFileReader(new InputStreamReader(input)).getContentIds();
			assertEquals("npm/npmjs/babel/code-frame/7.12.11", ids.get(0).toString());
			assertEquals("npm/npmjs/babel/code-frame/7.12.13", ids.get(1).toString());
			assertEquals("npm/npmjs/-/node-environment-flags/1.0.6", ids.get(2).toString());
		}
	}

	@Test
	void testAllValidIds() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("/yarn.lock")) {
			var ids = new YarnLockFileReader(new InputStreamReader(input)).getContentIds();
			assertTrue(ids.stream().allMatch(each -> each.isValid()));
		}
	}

}
