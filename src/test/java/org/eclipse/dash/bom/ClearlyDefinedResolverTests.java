package org.eclipse.dash.bom;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ClearlyDefinedResolverTests {
	
	String queryJSON = "[\n" + 
			"\"npm/npmjs/-/write/1.0.3\",\n" + 
			"\"npm/npmjs/-/ws/7.1.2\",\n" + 
			"\"npm/npmjs/-/xml-name-validator/3.0.0\",\n" + 
			"\"npm/npmjs/-/xmlchars/2.2.0\",\n" + 
			"\"npm/npmjs/-/xtend/4.0.2\",\n" + 
			"\"npm/npmjs/-/y18n/4.0.0\",\n" + 
			"\"npm/npmjs/-/yallist/2.1.2\",\n" + 
			"\"npm/npmjs/-/yargs/13.2.2\",\n" + 
			"\"npm/npmjs/-/yargs-parser/13.0.0\",\n" + 
			"\"npm/npmjs/-/yargs-unparser/1.5.0\",\n" + 
			"\"npm/npmjs/-/get-caller-file/1.0.3\",\n" + 
			"\"npm/npmjs/-/require-main-filename/1.0.1\",\n" + 
			"\"npm/npmjs/-/yargs/12.0.5\",\n" + 
			"\"npm/npmjs/-/yargs-parser/11.1.1\",\n" + 
			"\"npm/npmjs/-/yn/3.1.1\"\n" + 
			"]";

	@Test
	void test() {
		ClearlyDefinedResolver resolver = new ClearlyDefinedResolver();
		resolver.resolve(new ContentId("maven/mavencentral/org.junit.jupiter/junit-jupiter/5.5.5"));
	}

}
