package org.eclipse.dash.licenses.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.eclipse.dash.licenses.IContentId;

public class JsonUtils {
	public static String toJson(Collection<IContentId> ids) {
		// TODO Consider generalizing
		JsonArrayBuilder builder = Json.createBuilderFactory(null).createArrayBuilder();
		ids.stream().forEach(id -> builder.add(id.toString()));
		String json = builder.build().toString();
		return json;
	}

	public static JsonObject readJson(InputStream content) throws UnsupportedEncodingException {
		JsonReader reader = Json.createReader(new InputStreamReader(content, "UTF-8"));
		JsonObject json = (JsonObject) reader.read();
		return json;
	}
}
