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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.eclipse.dash.licenses.IContentId;
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

	@Test
	void testSingleEntry() throws IOException {

		// @formatter:off
		String contents = 
			"spawn-command@^0.0.2-1:\n" 
			+ "  version \"0.0.2-1\"\n"
			+ "  resolved \"https://registry.yarnpkg.com/spawn-command/-/spawn-command-0.0.2-1.tgz#62f5e9466981c1b796dc5929937e11c9c6921bd0\"\n"
			+ "  integrity sha1-YvXpRmmBwbeW3Fkpk34RycaSG9A=\n" 
			+ "";
		// @formatter:on

		var ids = new YarnLockFileReader(new StringReader(contents)).getContentIds();
		IContentId id = ids.get(0);

		assertEquals("-", id.getNamespace());
		assertEquals("spawn-command", id.getName());
		assertEquals("0.0.2-1", id.getVersion());
	}

	@Test
	void testInvalidEntry() throws IOException {

		// @formatter:off
		String contents = 
			"spawn-command@^0.0.2-1:\n" 
			+ "  resolved \"https://registry.yarnpkg.com/spawn-command/-/spawn-command-0.0.2-1.tgz#62f5e9466981c1b796dc5929937e11c9c6921bd0\"\n"
			+ "  integrity sha1-YvXpRmmBwbeW3Fkpk34RycaSG9A=\n" 
			+ "";
		// @formatter:on

		var ids = new YarnLockFileReader(new StringReader(contents)).getContentIds();
		IContentId id = ids.get(0);

		assertFalse(id.isValid());
	}
}
