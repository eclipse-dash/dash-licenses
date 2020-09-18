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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentId;
import org.junit.jupiter.api.Test;

class ContentIdTests {

	@Test
	void testValid() {
		IContentId id = ContentId.getContentId("npm/npmjs/-/write/1.0.3");
		assertEquals("npm/npmjs/-/write/1.0.3", id.toString());
	}

	@Test
	void testInvalid() {
		assertNull(ContentId.getContentId("write/1.0.3"));
	}

}
