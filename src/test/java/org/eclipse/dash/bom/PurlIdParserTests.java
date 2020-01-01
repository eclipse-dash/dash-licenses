package org.eclipse.dash.bom;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.dash.licenses.PurlIdParser;
import org.junit.jupiter.api.Test;

class PurlIdParserTests {

	@Test
	void testValid() {
		assertEquals("npm/npmjs/@babel/highlight/7.5.0", new PurlIdParser().parseId("@babel/highlight@7.5.0").get().toString());
		assertEquals("npm/npmjs/-/highlight/7.5.0", new PurlIdParser().parseId("highlight@7.5.0").get().toString());
	}
}
