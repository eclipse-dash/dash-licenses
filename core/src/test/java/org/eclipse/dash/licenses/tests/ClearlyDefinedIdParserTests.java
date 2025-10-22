/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.eclipse.dash.licenses.ClearlyDefinedIdParser;
import org.eclipse.dash.licenses.IContentId;
import org.junit.jupiter.api.Test;

class ClearlyDefinedIdParserTests {

	@Test
	void testBasic() {
		IContentId id = new ClearlyDefinedIdParser().parseId("npm/npmjs/stuff/shortid/2.2.8");
		assertEquals("npm/npmjs/stuff/shortid/2.2.8", id.toString());
		assertEquals("npm", id.getType());
		assertEquals("npmjs", id.getSource());
		assertEquals("stuff", id.getNamespace());
		assertEquals("shortid", id.getName());
		assertEquals("2.2.8", id.getVersion());
	}
	
	@Test
	void testValid() {
		String[] valid = {
				"git/github/metayeti/mINI/0.9.14",
				
				"maven/mavencentral/org.apache.maven/maven-resolver-provider/4.0.0-rc-4",
				
				"npm/npmjs/-/shortid/2.2.8",
				"npm/npmjs/@algolia/requester-browser-xhr/5.36.0",
				"npm/npmjs/@rspack/binding-darwin-x64/1.5.0",
				
				"pypi/pypi/-/parse-type/0.6.6"
		};

		Arrays.stream(valid).forEach(each -> {
			var id = new ClearlyDefinedIdParser().parseId(each);
			assertNotNull(id, each);
			assertEquals(each, id.toString(), each);
		});
	}
	
	@Test
	void testNonStandardButValid() {
		String[] valid = {
				"-/-/vscode/language-features-subset/1.104.0",
				
				"haskell/hackage/-/bytestring/0.12.2.0",
				
				"p2/orbit/p2.eclipse.plugin/assertj-core/3.27.4",
				
				"project/automotive.tractusx/-/tractusx-edc-kafka-extension/0.0",
				"project/technology.tm4e/-/vscode-syntax-files/1.104.0"
		};
		
		Arrays.stream(valid).forEach(each -> {
			var id = new ClearlyDefinedIdParser().parseId(each);
			assertNotNull(id, each);
			assertEquals(each, id.toString(), each);
		});
	}


	@Test
	void testInvalid() {
		assertNull(new ClearlyDefinedIdParser().parseId("npm/npmjs/stuff/shortid/"), "Version must be present.");
		assertNull(new ClearlyDefinedIdParser().parseId("npm /npmjs/stuff/shortid/2.2.8"), "Must not have spaces in the identifier.");
		assertNull(new ClearlyDefinedIdParser().parseId("npm/npmjs/shortid/2.2.8"), "Must have five segements.");
		assertNull(new ClearlyDefinedIdParser().parseId("pkg:github/itm/shawn@45ff42d775dd72ab6819524988031719a75b206a#src/apps/tcpip"), "Don't misinterpret a pURL.");
		assertNull(new ClearlyDefinedIdParser().parseId("p2/orbit/p2.eclipse-plugin/org.junit.jupiter.params/5.7.1.v20210222-1948, unknown, restricted, none"));
	}
	
	/**
	 * The <code>bunch_of_clearlydefined_ids.txt</code> file contains, well... a
	 * bunch of ClearlyDefined IDs that were extracted from our curation data. The
	 * general idea is to cast a broad of a net as possible to test changes to the
	 * parser.
	 */
	@Test
	void testValid2() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("/bunch_of_clearlydefined_ids.txt")) {
			var reader = new BufferedReader(new InputStreamReader(input));
			reader.lines().forEach(each -> {
				var id = new ClearlyDefinedIdParser().parseId(each);
				assertNotNull(id, each);
				assertEquals(each, id.toString(), each);
			});
		}
	}
}
