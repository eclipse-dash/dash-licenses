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

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.eclipse.dash.licenses.IContentId;

public final class JsonUtils {
	public static String toJson(Collection<IContentId> ids) {
		// TODO Consider generalizing
		JsonArrayBuilder builder = Json.createBuilderFactory(null).createArrayBuilder();
		ids.stream().forEach(id -> builder.add(id.toString()));
		String json = builder.build().toString();
		return json;
	}

	public static JsonObject readJson(InputStream content) {
		return readJson(new InputStreamReader(content, StandardCharsets.UTF_8));
	}

	public static JsonObject readJson(Reader content) {
		JsonReader reader = Json.createReader(content);
		JsonObject json = (JsonObject) reader.read();
		return json;
	}
}
