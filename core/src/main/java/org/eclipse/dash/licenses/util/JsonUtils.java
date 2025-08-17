/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.eclipse.dash.licenses.IContentId;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public final class JsonUtils {
	public static String toJson(Collection<IContentId> ids) {
		// TODO Consider generalizing
		JsonArrayBuilder builder = Json.createBuilderFactory(null).createArrayBuilder();
		ids.stream().forEach(id -> builder.add(id.toClearlyDefined()));
		String json = builder.build().toString();
		return json;
	}

	public static JsonObject readJson(InputStream content) {
		return readJson(new InputStreamReader(content, StandardCharsets.UTF_8));
	}

	public static JsonObject readJson(Reader content) {
		JsonReader reader = Json.createReader(content);
		return reader.read().asJsonObject();
	}

	public static JsonArray readJsonArray(InputStream content) {
		return readJsonArray(new InputStreamReader(content, StandardCharsets.UTF_8));
	}

	public static JsonArray readJsonArray(Reader content) {
		JsonReader reader = Json.createReader(content);
		return reader.read().asJsonArray();
	}
}
