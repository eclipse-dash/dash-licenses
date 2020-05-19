package org.eclipse.dash.licenses.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.eclipse.dash.licenses.IContentId;

public class JsonUtils {
	public static String toJson(Collection<IContentId> ids) {
		// TODO Consider generalizing
		JsonArrayBuilder builder = Json.createBuilderFactory(null).createArrayBuilder();
		ids.stream().forEach(id -> builder.add(id.toString()));
		String json = builder.build().toString();
		return json;
	}

	public static JsonObject readJson(InputStream content) {
		JsonReader reader = Json.createReader(new InputStreamReader(content, StandardCharsets.UTF_8));
		JsonObject json = (JsonObject) reader.read();
		return json;
	}

	/**
	 * Answers the {@link JsonArray} instance found on the path defined by the
	 * sequence of keys. That is, this method navigates a JsonObject to locate an
	 * array nested in the structure. The path is specified as a dynamic array of
	 * {@link String} parameters that map to nodes in the structure. This
	 * implementation assumes that the entire path to the array is composed of
	 * {@link JsonObject} instances with named properties. In fact, the
	 * implementation returns the very first array that it finds while descending
	 * through the structure (so if the array you want is actually nested in another
	 * array, a single call won't get you there).
	 * 
	 * For example, given the JSON structure:
	 * 
	 * <pre>
	 * {
	 * 	"name":"Wayne",
	 * 	"address":{
	 * 		"streetAddress":"2934 Baseline Road, Suite 202",
	 * 		"city":"Ottawa"
	 * 	},
	 * 	"random":{
	 * 		"stuff":{
	 * 			"nested":["one","two"]
	 * 		}
	 * 	}
	 * }
	 * </pre>
	 * 
	 * The array is obtained via:
	 * 
	 * <pre>
	 * JsonUtils.getJsonArray(json, "random", "stuff", "nested");
	 * </pre>
	 * 
	 * Note that this method always returns a value. If the value is missing or any
	 * key is invalid, an empty {@link JsonArray} is returned.
	 * 
	 * @param data An instance of {@link JsonObject}
	 * @param keys An array of {@link String} keys defining a path to a node.
	 * @return An instance of {@link JsonArray}
	 */
	public static JsonArray getJsonArray(JsonObject data, String... keys) {
		return getJsonArray(data, Arrays.stream(keys).iterator());
	}

	/**
	 * @see #getJsonArray(JsonObject, String...)
	 * 
	 * @param data An instance of {@link JsonObject}
	 * @param keys An {@link Iterator} of {@link String} keys defining a path to a
	 *             node.
	 * @return An instance of {@link JsonArray}
	 */
	public static JsonArray getJsonArray(JsonObject data, Iterator<String> keys) {
		if (keys.hasNext()) {
			JsonValue value = data.get(keys.next());
			if (value != null) {
				if (value.getValueType() == JsonValue.ValueType.ARRAY)
					return value.asJsonArray();
				if (value.getValueType() == JsonValue.ValueType.OBJECT)
					return getJsonArray(value.asJsonObject(), keys);
			}
		}
		return Json.createArrayBuilder().build();
	}
}
