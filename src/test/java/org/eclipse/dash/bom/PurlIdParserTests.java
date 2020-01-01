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

import org.eclipse.dash.licenses.PurlIdParser;
import org.junit.jupiter.api.Test;

class PurlIdParserTests {

	@Test
	void testValid() {
		assertEquals("npm/npmjs/@babel/highlight/7.5.0", new PurlIdParser().parseId("@babel/highlight@7.5.0").get().toString());
		assertEquals("npm/npmjs/-/highlight/7.5.0", new PurlIdParser().parseId("highlight@7.5.0").get().toString());
	}
}
