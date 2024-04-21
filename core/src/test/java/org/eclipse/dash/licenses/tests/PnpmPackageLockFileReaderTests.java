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

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.cli.PnpmPackageLockFileReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PnpmPackageLockFileReaderTests {

	@Test
	void testNoPackages() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("fixtures/pnpm/pnpm-lock-no-packages.yaml")) {
			PnpmPackageLockFileReader reader = new PnpmPackageLockFileReader(input);
			var ids = reader.getContentIds();
			assertTrue(ids.isEmpty());
		}
	}

	@Test
	void testDuplicates() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("fixtures/pnpm/pnpm-lock-duplicate.yaml")) {
			PnpmPackageLockFileReader reader = new PnpmPackageLockFileReader(input);
			var ids = reader.getContentIds();
			assertEquals(1, ids.size());
			assertEquals("npm/npmjs/@babel/preset-modules/0.1.6-no-external-plugins", ids.iterator().next().toString());
		}
	}

	@Test
	void testV5Format() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("fixtures/pnpm/pnpm-lock-v5.yaml")) {
			PnpmPackageLockFileReader reader = new PnpmPackageLockFileReader(input);
			var ids = reader.getContentIds();

			assertEquals(12, ids.size());

			// Test that a handful of content ids are detected as expected.
			var includes = new IContentId[] { ContentId.getContentId("npm", "npmjs", "-", "graceful-fs", "4.2.2"),
					ContentId.getContentId("npm", "npmjs", "-", "pify", "3.0.0"),
					ContentId.getContentId("npm", "npmjs", "-", "write-json-file", "2.3.0") };

			for (IContentId id : includes) {
				assertTrue(ids.contains(id), "Should include: " + id);
			}
		}
	}

	@Test
	void testV6Format() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("fixtures/pnpm/pnpm-lock-v6.yaml")) {
			PnpmPackageLockFileReader reader = new PnpmPackageLockFileReader(input);
			var ids = reader.getContentIds();

			assertEquals(579, ids.size());

			// Test that a handful of content ids are detected as expected.
			var includes = new IContentId[] { ContentId.getContentId("npm", "npmjs", "@babel", "code-frame", "7.18.6"),
					ContentId.getContentId("npm", "npmjs", "-", "git-semver-tags", "4.1.1"),
					ContentId.getContentId("npm", "npmjs", "-", "yargs", "17.6.2") };

            for (IContentId id : includes) {
                assertTrue(ids.contains(id), "Should include: " + id);
            }
		}
	}

	@Test
	void testV9Format() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("fixtures/pnpm/pnpm-lock-v9.yaml")) {
			PnpmPackageLockFileReader reader = new PnpmPackageLockFileReader(input);
			var ids = reader.getContentIds();

			assertEquals(12, ids.size());

			// Test that a handful of content ids are detected as expected.
			var includes = new IContentId[] { ContentId.getContentId("npm", "npmjs", "-", "graceful-fs", "4.2.2"),
					ContentId.getContentId("npm", "npmjs", "-", "pify", "3.0.0"),
					ContentId.getContentId("npm", "npmjs", "-", "write-json-file", "2.3.0") };

			for (IContentId id : includes) {
				assertTrue(ids.contains(id), "Should include: " + id);
			}
		}
	}

	@Test
	void testFormat() throws IOException {
		//try (InputStream input = this.getClass().getResourceAsStream("/pnpm-lock.yaml")) {
		//try (InputStream input = this.getClass().getResourceAsStream("/pnpm-lock2.yaml")) {
		try (InputStream input = this.getClass().getResourceAsStream("fixtures/pnpm/pnpm-lock-v6.yaml")) {
			PnpmPackageLockFileReader reader = new PnpmPackageLockFileReader(input);
			//var ids = reader.contentIds().collect(Collectors.toList());
			var ids = reader.getContentIds();
            assertFalse(ids.isEmpty(), "Should have some content ids");

		}
	}

	@Test
	void testAllRecordsDetected() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("fixtures/pnpm/pnpm-lock-v6-small.yaml")) {
			PnpmPackageLockFileReader reader = new PnpmPackageLockFileReader(input);

			String[] expected = { "npm/npmjs/-/git-semver-tags/4.1.1", "npm/npmjs/@babel/code-frame/7.18.6", "npm/npmjs/@babel/preset-modules/0.1.6-no-external-plugins"};
			Arrays.sort(expected);
			String[] found = reader.contentIds().map(IContentId::toString).sorted().toArray(String[]::new);
			assertArrayEquals(expected, found);
		}
	}

	@Test
	void shouldReturnErrorForInvalidYamlfile() {
		InputStream input = new ByteArrayInputStream("invalid".getBytes(Charset.defaultCharset()));
		PnpmPackageLockFileReader reader = new PnpmPackageLockFileReader(input);

		Exception exception = assertThrows(RuntimeException.class, reader::getContentIds);
		assertEquals("Error reading content of package-lock.yaml file", exception.getMessage());
	}

	@Test
	void shouldReturnErrorForEmptyfile() {
		InputStream input = new ByteArrayInputStream("".getBytes(Charset.defaultCharset()));
		PnpmPackageLockFileReader reader = new PnpmPackageLockFileReader(input);

		Exception exception = assertThrows(RuntimeException.class, reader::getContentIds);
		assertEquals("Error reading content of package-lock.yaml file", exception.getMessage());
	}

}
