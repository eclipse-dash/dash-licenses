package org.eclipse.dash.bom;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ContentIdTests {

	@Test
	void test() {
		IContentId id = new ContentId("npm/npmjs/-/write/1.0.3");
		assertEquals("npm/npmjs/-/write/1.0.3",id.toString());
	}

}
