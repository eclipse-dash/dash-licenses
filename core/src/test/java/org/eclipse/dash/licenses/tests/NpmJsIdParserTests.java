/*************************************************************************
 * Copyright (c) 2019 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.dash.licenses.NpmJsIdParser;
import org.junit.jupiter.api.Test;

class NpmJsIdParserTests {

	@Test
	void testBasic() {
		assertEquals("npm/npmjs/-/highlight/7.5.0", new NpmJsIdParser().parseId("highlight@7.5.0").toString());
	}

	@Test
	void testWithScope() {
		assertEquals("npm/npmjs/@babel/highlight/7.5.0",
				new NpmJsIdParser().parseId("@babel/highlight@7.5.0").toString());
	}

	@Test
	void testWithDashScope() {
		assertEquals("npm/npmjs/-/highlight/7.5.0", new NpmJsIdParser().parseId("-/highlight@7.5.0").toString());
	}

	@Test
	void testWithDashes() {
		assertEquals("npm/npmjs/-/high-light/7.5.0", new NpmJsIdParser().parseId("-/high-light@7.5.0").toString());
	}

	@Test
	void testWithFile() {
		assertNull(new NpmJsIdParser()
				.parseId("vscode-css-languageserver@file:target/vscode-css-languageserver-1.0.0.tgz"));
	}

	@Test
	void testWithPartialVersion() {
		assertNull(new NpmJsIdParser().parseId("cheerio@1.0"));
	}

	@Test
	void testWithInvalidVersion() {
		assertNull(new NpmJsIdParser().parseId("cheerio@1.0."));
		assertNull(new NpmJsIdParser().parseId("cheerio@a.0."));
		assertNull(new NpmJsIdParser().parseId("cheerio@.0."));
	}

	@Test
	void testWithValidVersion() {
		assertEquals("1.0.0", new NpmJsIdParser().parseId("cheerio@1.0.0").getVersion());
		assertEquals("12.34.56", new NpmJsIdParser().parseId("cheerio@12.34.56").getVersion());
		assertEquals("1.0.0-rc.3", new NpmJsIdParser().parseId("cheerio@1.0.0-rc.3").getVersion());
	}

	@Test
	void testMissingName() {
		assertNull(new NpmJsIdParser().parseId("@1.0.0-rc.3"));
	}

	@Test
	void testMissingVersion() {
		assertNull(new NpmJsIdParser().parseId("cheerio@"));
	}

	@Test
	void testExtraInformationFails() {
		assertNull(new NpmJsIdParser().parseId("blah/blah/blah@1.2.3"));
	}
}
