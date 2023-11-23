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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.cli.PackageLockFileReader;
import org.junit.jupiter.api.Test;

class PackageLockFileReaderTests {

	private static final String PACKAGE_LOCK_JSON = "/test_data_package-lock.json";
	private static final String PACKAGE_LOCK_V2_JSON = "/test_data_package-lock-v2.json";

	@Test
	void testV1Format() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream(PACKAGE_LOCK_JSON)) {
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
		try (InputStream input = this.getClass().getResourceAsStream(PACKAGE_LOCK_V2_JSON)) {
			PackageLockFileReader reader = new PackageLockFileReader(input);
			// This "test" is a little... abridged. At least this test proves
			// that we're getting something in the right format from the reader
			// without having to enumerate all 574 (I think) records).
			String[] expected = { "npm/npmjs/@babel/code-frame/7.12.13", "npm/npmjs/@babel/compat-data/7.13.15",
					"npm/npmjs/@babel/core/7.13.15" };
			String[] found = reader
					.getContentIds()
					.stream()
					.limit(3)
					.map(IContentId::toString)
					.sorted()
					.toArray(String[]::new);
			assertArrayEquals(expected, found);
		}
	}

	@Test
	void testV3Format() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("/test_data_package-lock-v3.json")) {
			PackageLockFileReader reader = new PackageLockFileReader(input);
			var ids = reader.contentIds().collect(Collectors.toList());

			assertEquals(769, ids.size());

			// Issue #285 Component name is remapped. Make sure that we don't see the key
			// in the results. This record should manifest as langium-statemachine-dsl (see
			// below)
			assertFalse(ids.stream().anyMatch(each -> "statemachine".equals(each.getName())));

			// Test that a handful of content ids are detected as expected.
			var includes = new IContentId[] { ContentId.getContentId("npm", "npmjs", "-", "ansi-styles", "3.2.1"),
					ContentId.getContentId("npm", "npmjs", "@typescript-eslint", "eslint-plugin", "6.4.1"),
					ContentId.getContentId("npm", "npmjs", "@types", "minimatch", "3.0.5"),
					ContentId.getContentId("npm", "local", "-", "langium-requirements-dsl", "2.1.0"),
					ContentId.getContentId("npm", "local", "-", "langium-domainmodel-dsl", "2.1.0"),
					ContentId.getContentId("npm", "local", "-", "langium-statemachine-dsl", "2.1.0") };

			for (int index = 0; index < includes.length; index++) {
				var id = includes[index];
				assertTrue("Should include: " + id.toString(), ids.contains(id));
			}
		}
	}

	@Test
	void testAllRecordsDetected() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("/differentResolved.json")) {
			PackageLockFileReader reader = new PackageLockFileReader(input);

			String[] expected = { "npm/npmjs/@babel/code-frame/7.12.13", "npm/local/-/some_local_package/1.2.3", };
			Arrays.sort(expected);
			String[] found = reader.contentIds().map(IContentId::toString).sorted().toArray(String[]::new);
			assertArrayEquals(expected, found);
		}
	}
}
