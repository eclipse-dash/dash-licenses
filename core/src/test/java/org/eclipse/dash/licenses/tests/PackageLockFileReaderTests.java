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
			// This "test" is a little... abridged. At least this test proves
			// that we're getting something in the right format from the reader
			// without having to enumerate all 574 (I think) records).
			String[] expected = { "npm/npmjs/@babel/code-frame/7.12.13", "npm/npmjs/@babel/compat-data/7.13.15",
					"npm/npmjs/@babel/core/7.13.15" };
			String[] found = reader.getContentIds().stream().limit(3).map(IContentId::toString).sorted()
					.toArray(String[]::new);
			assertArrayEquals(expected, found);
		}
	}
}
