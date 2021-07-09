/*************************************************************************
 * Copyright (c) 2020, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;

import org.eclipse.dash.licenses.LicenseSupport.Status;
import org.eclipse.dash.licenses.foundation.FoundationData;
import org.eclipse.dash.licenses.util.JsonUtils;
import org.junit.jupiter.api.Test;

class FoundationDataTests {
	@Test
	void testStringConfidence() {
		// @formatter:off
		String json =
			"{\n" +
			"	\"authority\": \"CQ7766\", \n" +
			"	\"confidence\": \"100\", \n" +
			"	\"definitionUrl\": null, \n" +
			"	\"id\": \"maven/mavencentral/com.google.guava/guava/15.0\", \n" +
			"	\"license\": \"Apache-2.0\", \n" +
			"	\"sourceUrl\": null, \n" +
			"	\"status\": \"approved\"\n" +
			"}\n";
		// @formatter:on

		FoundationData data = new FoundationData(JsonUtils.readJson(new StringReader(json)));
		assertEquals("https://dev.eclipse.org/ipzilla/show_bug.cgi?id=7766", data.getUrl());
		assertEquals(Status.Approved, data.getStatus());
		assertEquals(100, data.getScore());
	}

	@Test
	void testNumericConfidence() {
		// @formatter:off
		String json =
			"{\n" +
			"	\"authority\": \"CQ7766\", \n" +
			"	\"confidence\": 100, \n" +
			"	\"definitionUrl\": null, \n" +
			"	\"id\": \"maven/mavencentral/com.google.guava/guava/15.0\", \n" +
			"	\"license\": \"Apache-2.0\", \n" +
			"	\"sourceUrl\": null, \n" +
			"	\"status\": \"approved\"\n" +
			"}\n";
		// @formatter:on

		FoundationData data = new FoundationData(JsonUtils.readJson(new StringReader(json)));
		assertEquals("https://dev.eclipse.org/ipzilla/show_bug.cgi?id=7766", data.getUrl());
		assertEquals(Status.Approved, data.getStatus());
		assertEquals(100, data.getScore());
	}

	@Test
	void testIPLabAuthority() {
		// @formatter:off
		String json =
			"{\n" +
			"	\"authority\": \"#78\", \n" +
			"	\"confidence\": 100, \n" +
			"	\"definitionUrl\": null, \n" +
			"	\"id\": \"maven/mavencentral/com.google.guava/guava/15.0\", \n" +
			"	\"license\": \"Apache-2.0\", \n" +
			"	\"sourceUrl\": null, \n" +
			"	\"status\": \"approved\"\n" +
			"}\n";
		// @formatter:on

		FoundationData data = new FoundationData(JsonUtils.readJson(new StringReader(json)));
		assertEquals("https://gitlab.eclipse.org/eclipsefdn/emo-team/iplab/-/issues/78", data.getUrl());
		assertEquals(Status.Approved, data.getStatus());
		assertEquals(100, data.getScore());
	}
}
