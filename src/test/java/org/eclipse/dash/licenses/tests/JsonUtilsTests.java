package org.eclipse.dash.licenses.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;

import org.eclipse.dash.licenses.util.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonUtilsTests {
	private JsonObject data;

	@Test
	void testGetJsonObjectStringArray() {
		assertEquals("one", JsonUtils.getJsonArray(data, "random", "stuff", "nested").getString(0));
	}

	@Test
	void testMissing() {
		assertTrue(JsonUtils.getJsonArray(data, "random", "random", "stuff").isEmpty());
		assertTrue(JsonUtils.getJsonArray(data, "name", "address", "city").isEmpty());
		assertTrue(JsonUtils.getJsonArray(data, "missing").isEmpty());
	}

	@BeforeEach
	public void setup() {
		// @formatter:off
		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		data = factory.createObjectBuilder().add("name", "Wayne")
			.add("address",
				factory.createObjectBuilder()
					.add("streetAddress", "2934 Baseline Road, Suite 202")
					.add("city", "Ottawa"))
			.add("random", 
				factory.createObjectBuilder()
					.add("stuff", 
						factory.createObjectBuilder()
							.add("nested",
								factory.createArrayBuilder()
									.add("one")
									.add("two"))))
			.build();
		//@formatter:on
	}

}
