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

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedContentData;
import org.junit.jupiter.api.Test;

class ClearlyDefinedContentDataTests {
	@Test
	void test() throws Exception {
		InputStream input = this.getClass().getResourceAsStream("/write-1.0.3.json");
		JsonReader reader = Json.createReader(new InputStreamReader(input, "UTF-8"));
		JsonObject data = ((JsonValue) reader.read()).asJsonObject();
		ClearlyDefinedContentData info = new ClearlyDefinedContentData("npm/npmjs/-/write/1.0.3", data);

		assertEquals("npm/npmjs/-/write/1.0.3", info.getId().toString());
		assertEquals("MIT", info.getLicense());
		assertEquals(97, info.getScore());
	}
}
