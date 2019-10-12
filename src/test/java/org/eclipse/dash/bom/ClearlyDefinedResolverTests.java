package org.eclipse.dash.bom;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ClearlyDefinedResolverTests {

	@Test
	void test() {
		ClearlyDefinedResolver resolver = new ClearlyDefinedResolver();
		resolver.resolve(new ContentId("maven/mavencentral/org.junit.jupiter/junit-jupiter/5.5.5"));
	}

}
