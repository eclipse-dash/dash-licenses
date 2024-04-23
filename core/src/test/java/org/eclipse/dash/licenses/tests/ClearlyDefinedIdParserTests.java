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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.dash.licenses.ClearlyDefinedIdParser;
import org.junit.jupiter.api.Test;

class ClearlyDefinedIdParserTests {

	@Test
	void testBasic() {
		assertEquals("npm/npmjs/-/shortid/2.2.8",
				new ClearlyDefinedIdParser().parseId("npm/npmjs/-/shortid/2.2.8").toString());
	}

	@Test
	void testInvalid1() {
		assertNull(new ClearlyDefinedIdParser().parseId("npm/npmjs/stuff/shortid/"));
	}

	@Test
	void testInvalid2() {
		assertNull(new ClearlyDefinedIdParser().parseId("npm/npmjs/shortid/2.2.8"));
	}

	@Test
	void testInvalid3() {
		assertNull(new ClearlyDefinedIdParser().parseId("npm /npmjs/stuff/shortid/2.2.8"));
	}

	@Test
	void testInvalid4() {
		assertNull(new ClearlyDefinedIdParser()
				.parseId(
						"p2/orbit/p2.eclipse-plugin/org.junit.jupiter.params/5.7.1.v20210222-1948, unknown, restricted, none"));
	}

	@Test
	void testNamespace() {
		assertEquals("stuff", new ClearlyDefinedIdParser().parseId("npm/npmjs/stuff/shortid/2.2.8").getNamespace());
	}
}
