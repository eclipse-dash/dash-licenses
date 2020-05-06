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

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.dash.licenses.NpmJsIdParser;
import org.junit.jupiter.api.Test;

class NpmJsIdParserTests {

	@Test
	void testBasic() {
		assertEquals("npm/npmjs/-/highlight/7.5.0", new NpmJsIdParser().parseId("highlight@7.5.0").get().toString());
	}

	@Test
	void testWithScope() {
		assertEquals("npm/npmjs/@babel/highlight/7.5.0",
				new NpmJsIdParser().parseId("@babel/highlight@7.5.0").get().toString());
	}

	@Test
	void testWithComplexVersion() {
		assertEquals("npm/npmjs/-/cheerio/1.0.0-rc.3",
				new NpmJsIdParser().parseId("cheerio@1.0.0-rc.3").get().toString());
	}

	@Test
	void testMissingName() {
		assertFalse(new NpmJsIdParser().parseId("@1.0.0-rc.3").isPresent());
	}

	@Test
	void testMissingVersion() {
		assertFalse(new NpmJsIdParser().parseId("cheerio@").isPresent());
	}
}
