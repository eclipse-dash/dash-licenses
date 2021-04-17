/*************************************************************************
 * Copyright (c) 2020,2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.cli.PackageLockFileReader;
import org.junit.jupiter.api.Test;

class PackageLockFileReaderTests {

	@Test
	void testV1Format() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("/package-lock.json")) {
			PackageLockFileReader reader = new PackageLockFileReader(input);
			String[] expected = { "npm/npmjs/-/loglevel/1.6.1", "npm/npmjs/-/sax/1.2.4", "npm/npmjs/-/saxes/3.1.9",
					"npm/npmjs/-/slimdom-sax-parser/1.1.3", "npm/npmjs/-/slimdom/2.2.1", "npm/npmjs/-/xml-js/1.6.11",
					"npm/npmjs/-/xmlchars/1.3.1", "npm/npmjs/@namespace/fontoxpath/3.3.0" };
			String[] found = reader.getContentIds().stream().map(IContentId::toString).sorted().toArray(String[]::new);
			assertArrayEquals(expected, found);
		}
	}

	@Test
	void testV2Format() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("/package-lock-v2.json")) {
			PackageLockFileReader reader = new PackageLockFileReader(input);
			String[] expected = { "npm/npmjs/-/delayed-stream/1.0.0", "npm/npmjs/-/is-descriptor/1.0.2",
					"npm/npmjs/@babel/code-frame/7.12.13", };
			String[] found = reader.getContentIds().stream().map(IContentId::toString).sorted().toArray(String[]::new);
			assertArrayEquals(expected, found);
		}
	}
}
