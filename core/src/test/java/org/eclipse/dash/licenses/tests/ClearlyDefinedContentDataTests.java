/*************************************************************************
 * Copyright (c) 2019,2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedContentData;
import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

class ClearlyDefinedContentDataTests {
	@Test
	void testSingleLicense() throws Exception {
		InputStream input = this.getClass().getResourceAsStream("/write-1.0.3.json");
		JsonReader reader = Json.createReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		JsonObject data = ((JsonValue) reader.read()).asJsonObject();
		ClearlyDefinedContentData info = new ClearlyDefinedContentData("npm/npmjs/-/write/1.0.3", data);

		assertEquals("npm/npmjs/-/write/1.0.3", info.getId().toString());
		assertEquals("MIT", info.getLicense());
		assertEquals("1.0.3", info.getRevision());
		assertArrayEquals(new String[] { "MIT" }, info.discoveredLicenses().toArray(String[]::new));
		assertEquals(94, info.getScore());
		assertEquals(97, info.getEffectiveScore());
		assertEquals("https://clearlydefined.io/definitions/npm/npmjs/-/write/1.0.3", info.getUrl());
		assertEquals("https://github.com/jonschlinkert/write/tree/f5397515060bf42f75151fcc3c4722517e4e322a",
				info.getSourceLocation().getUrl());
		assertEquals("https://github.com/jonschlinkert/write/archive/refs/tags/1.0.3.zip",
				info.getSourceLocation().getDownloadUrl());
		assertNull(info.getStatus());
	}

	@Test
	void testDiscoveredLicenses() throws Exception {
		InputStream input = this.getClass().getResourceAsStream("/lockfile-1.1.0.json");
		JsonReader reader = Json.createReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		JsonObject data = ((JsonValue) reader.read()).asJsonObject();
		ClearlyDefinedContentData info = new ClearlyDefinedContentData("npm/npmjs/-/lockfile/1.1.1", data);

		assertArrayEquals(new String[] { "GPL-2.0", "MIT" }, info.discoveredLicenses().toArray(String[]::new));
	}

	@Test
	void testMissingData() {
		ClearlyDefinedContentData info = new ClearlyDefinedContentData("test", JsonValue.EMPTY_JSON_OBJECT);
		assertEquals(0, info.getScore());
		assertEquals(0, info.getEffectiveScore());
		assertEquals("", info.getLicense());
		assertArrayEquals(new String[] {}, info.discoveredLicenses().toArray(String[]::new));
	}
}
