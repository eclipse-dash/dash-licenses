/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedContentData;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedSupport;
import org.eclipse.dash.licenses.cli.CommandLineSettings;
import org.eclipse.dash.licenses.util.JsonUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClearlyDefinedSupportTests {
	static ClearlyDefinedSupport clearlyDefinedSupport;

	@BeforeAll
	static void setup() {
		clearlyDefinedSupport = new ClearlyDefinedSupport(CommandLineSettings.getSettings(new String[] {}));
	}

	@Test
	void testMatchAgainstClearlyDefined() {
		List<IContentData> results = new ArrayList<>();
		// FIXME Reconfigure to run against fixed data on a local test server
		// This test is running against the live server. We should either (or both)
		// refactor to allow testing against a local file, or spin up a server
		// capable of (at least) faking the interaction in a consistent manner.
		clearlyDefinedSupport.queryLicenseData(Collections.singleton(ContentId.getContentId("npm/npmjs/-/write/0.2.0")),
				data -> results.add(data));
		assertEquals(1, results.size());
		assertEquals("npm/npmjs/-/write/0.2.0", results.get(0).getId().toString());
		assertEquals("clearlydefined", results.get(0).getAuthority());
		assertEquals("MIT", results.get(0).getLicense());
	}

	@Test
	void testAcceptable() {
		// @formatter:off
		String json = 
				"{\n" + 
				"    \"licensed\": {\n" + 
				"        \"declared\": \"MIT\", \n" + 
				"        \"facets\": {\n" + 
				"            \"core\": {\n" + 
				"                \"discovered\": {\n" + 
				"                    \"expressions\": [\n" + 
				"                        \"MIT\"\n" + 
				"                    ] \n" + 
				"                }, \n" + 
				"                \"files\": 4\n" + 
				"            }\n" + 
				"        }, \n" + 
				"        \"score\": {\n" + 
				"            \"total\": 80\n" + 
				"        }\n" + 
				"    }\n" +
				"}\n";
		// @formatter:on

		ClearlyDefinedContentData data = new ClearlyDefinedContentData("id",
				JsonUtils.readJson(new StringReader(json)));

		assertTrue(clearlyDefinedSupport.isAccepted(data));

	}

	@Test
	void testUnacceptableByScore() {
		// @formatter:off
		String json = 
				"{\n" + 
				"    \"licensed\": {\n" + 
				"        \"declared\": \"MIT\", \n" + 
				"        \"facets\": {\n" + 
				"            \"core\": {\n" + 
				"                \"discovered\": {\n" + 
				"                    \"expressions\": [\n" + 
				"                        \"MIT\"\n" + 
				"                    ] \n" + 
				"                }, \n" + 
				"                \"files\": 4\n" + 
				"            }\n" + 
				"        }, \n" + 
				"        \"score\": {\n" + 
				"            \"total\": 50\n" + 
				"        }\n" + 
				"    }\n" +
				"}\n";
		// @formatter:on

		ClearlyDefinedContentData data = new ClearlyDefinedContentData("id",
				JsonUtils.readJson(new StringReader(json)));

		assertFalse(clearlyDefinedSupport.isAccepted(data));

	}

	@Test
	void testUnacceptableByDiscoveredLicense() {
		// @formatter:off
		String json = 
				"{\n" + 
				"    \"licensed\": {\n" + 
				"        \"declared\": \"MIT\", \n" + 
				"        \"facets\": {\n" + 
				"            \"core\": {\n" + 
				"                \"discovered\": {\n" + 
				"                    \"expressions\": [\n" + 
				"                        \"MIT\",\n" + 
				"                        \"GPL-2.0\"\n" + 
				"                    ] \n" + 
				"                }, \n" + 
				"                \"files\": 4\n" + 
				"            }\n" + 
				"        }, \n" + 
				"        \"score\": {\n" + 
				"            \"total\": 80\n" + 
				"        }\n" + 
				"    }\n" +
				"}\n";
		// @formatter:on

		ClearlyDefinedContentData data = new ClearlyDefinedContentData("id",
				JsonUtils.readJson(new StringReader(json)));

		assertFalse(clearlyDefinedSupport.isAccepted(data));
	}
}
