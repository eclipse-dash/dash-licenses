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

	private static final String YARN_LOCK = "/test_data_yarn.lock";

	@Test
	void test() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream(YARN_LOCK)) {
			var ids = new YarnLockFileReader(new InputStreamReader(input)).getContentIds();
			assertEquals("npm/npmjs/@babel/code-frame/7.12.11", ids.get(0).toString());
			assertEquals("npm/npmjs/@babel/code-frame/7.12.13", ids.get(1).toString());
			assertEquals("npm/npmjs/-/node-environment-flags/1.0.6", ids.get(2).toString());
		}
	}

	@Test
	void testAllValidIds() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream(YARN_LOCK)) {
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
	void testSingleEntryWithNamespace() throws IOException {

		// @formatter:off
		String contents = 
			"\"@babel/code-frame@^7.0.0\", \"@babel/code-frame@^7.12.13\":\n"
			+ "  version \"7.12.13\"\n"
			+ "  resolved \"https://registry.yarnpkg.com/@babel/code-frame/-/code-frame-7.12.13.tgz#dcfc826beef65e75c50e21d3837d7d95798dd658\"\n"
			+ "  integrity sha512-HV1Cm0Q3ZrpCR93tkWOYiuYIgLxZXZFVG2VgK+MBWjUqZTundupbfx2aXarXuw5Ko5aMcjtJgbSs4vUGBS5v6g==\n"
			+ "  dependencies:\n"
			+ "    \"@babel/highlight\" \"^7.12.13\"";
		// @formatter:on

		var ids = new YarnLockFileReader(new StringReader(contents)).getContentIds();
		IContentId id = ids.get(0);

		assertEquals("@babel", id.getNamespace());
		assertEquals("code-frame", id.getName());
		assertEquals("7.12.13", id.getVersion());
	}

	@Test
	void testMultipleKeysWithoutQuotes() throws IOException {

		// @formatter:off
		String contents = 
			"code-frame@^7.0.0, @babel/code-frame@^7.12.13:\n"
			+ "  version \"7.12.13\"\n"
			+ "  resolved \"https://registry.yarnpkg.com/@babel/code-frame/-/code-frame-7.12.13.tgz#dcfc826beef65e75c50e21d3837d7d95798dd658\"\n"
			+ "  integrity sha512-HV1Cm0Q3ZrpCR93tkWOYiuYIgLxZXZFVG2VgK+MBWjUqZTundupbfx2aXarXuw5Ko5aMcjtJgbSs4vUGBS5v6g==\n"
			+ "  dependencies:\n"
			+ "    \"@babel/highlight\" \"^7.12.13\"";
		// @formatter:on

		var ids = new YarnLockFileReader(new StringReader(contents)).getContentIds();
		IContentId id = ids.get(0);

		assertEquals("-", id.getNamespace());
		assertEquals("code-frame", id.getName());
		assertEquals("7.12.13", id.getVersion());
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
