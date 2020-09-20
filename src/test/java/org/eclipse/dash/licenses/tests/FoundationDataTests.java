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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FoundationDataTests {

	private FoundationData data;

	@BeforeEach
	void setup() {
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

		data = new FoundationData(JsonUtils.readJson(new StringReader(json)));
	}

	@Test
	void testGetUrl() {
		assertEquals("https://dev.eclipse.org/ipzilla/show_bug.cgi?id=7766", data.getUrl());
	}

	@Test
	void testGetStatus() {
		assertEquals(Status.Approved, data.getStatus());
	}
}
