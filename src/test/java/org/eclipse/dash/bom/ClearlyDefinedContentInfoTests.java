package org.eclipse.dash.bom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

class ClearlyDefinedContentInfoTests {
	@Test
	void test() throws Exception {
		InputStream input = this.getClass().getResourceAsStream("/write-1.0.3.json");
		JSONObject data = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(input);
		ClearlyDefinedContentInfo info = new ClearlyDefinedContentInfo(data);
		assertEquals("MIT", info.getLicense().toString());
	}

}
