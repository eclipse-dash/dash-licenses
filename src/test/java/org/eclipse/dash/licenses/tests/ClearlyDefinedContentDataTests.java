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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedContentData;
import org.junit.jupiter.api.Test;

class ClearlyDefinedContentDataTests {
	@Test
	void testSingleLicense() throws Exception {
		InputStream input = this.getClass().getResourceAsStream("/write-1.0.3.json");
		JsonReader reader = Json.createReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		JsonObject data = ((JsonValue) reader.read()).asJsonObject();
		ClearlyDefinedContentData info = new ClearlyDefinedContentData("npm/npmjs/-/write/1.0.3", data);

		assertEquals("npm/npmjs/-/write/1.0.3", info.getId().toString());
		assertEquals("MIT", info.getLicense());
		assertArrayEquals(new String[] { "MIT" }, info.discoveredLicenses().toArray(String[]::new));
		assertEquals(94, info.getScore());
		assertEquals(97, info.getEffectiveScore());
		assertEquals("https://clearlydefined.io/definitions/npm/npmjs/-/write/1.0.3", info.getUrl());
		assertNull(info.getStatus());
	}

	@Test
	void testDiscoveredLicenses() throws Exception {
		InputStream input = this.getClass().getResourceAsStream("/lockfile-1.1.0.json");
		JsonReader reader = Json.createReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		JsonObject data = ((JsonValue) reader.read()).asJsonObject();
		ClearlyDefinedContentData info = new ClearlyDefinedContentData("npm/npmjs/-/lockfile/1.1.1", data);

		assertArrayEquals(new String[] { "BSD-2-Clause", "MIT" }, info.discoveredLicenses().toArray(String[]::new));
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
