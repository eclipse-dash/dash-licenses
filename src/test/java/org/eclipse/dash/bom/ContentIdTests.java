package org.eclipse.dash.bom;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ContentIdTests {

	@Test
	void testValid() {
		IContentId id = ContentId.getContentId("npm/npmjs/-/write/1.0.3");
		assertEquals("npm/npmjs/-/write/1.0.3",id.toString());
	}
	
	@Test
	void testInvalid() {
		assertThrows(IllegalArgumentException.class, () -> ContentId.getContentId("write/1.0.3"));
	}

}
