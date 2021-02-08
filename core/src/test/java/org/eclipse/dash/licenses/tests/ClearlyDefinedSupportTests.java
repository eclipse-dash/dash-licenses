/*************************************************************************
 * Copyright (c) 2019,2021 The Eclipse Foundation and others.
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedContentData;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedSupport;
import org.eclipse.dash.licenses.context.IContext;
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.eclipse.dash.licenses.util.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClearlyDefinedSupportTests {

	private IContext context;

	@BeforeEach
	void setup() {
		context = new IContext() {
			@Override
			public ISettings getSettings() {
				return new ISettings() {

				};
			}

			@Override
			public LicenseSupport getLicenseService() {
				return LicenseSupport.getLicenseSupport(new StringReader("{}"));
			}

			@Override
			public ILicenseDataProvider getClearlyDefinedService() {
				return new ClearlyDefinedSupport(this);
			}

			@Override
			public IHttpClientService getHttpClientService() {
				return new IHttpClientService() {
					@Override
					public int post(String url, String contentType, String payload, Consumer<String> handler) {
						String content = new BufferedReader(new InputStreamReader(
								this.getClass().getResourceAsStream("/write-1.0.3.json"), StandardCharsets.UTF_8))
										.lines().collect(Collectors.joining("\n"));

						// The file contains only the information for the one record; the
						// ClearlyDefined service expects a Json collection as the response,
						// so insert the file contents into an array and pass that value to
						// the handler.
						var builder = new StringBuilder();
						builder.append("{");
						builder.append("\"npm/npmjs/-/write/0.2.0\": ");
						builder.append(content);
						builder.append("}");

						handler.accept(builder.toString());
						return 200;
					}
				};
			}
		};
	}

	@Test
	void testMatchAgainstClearlyDefined() {
		List<IContentData> results = new ArrayList<>();
		context.getClearlyDefinedService().queryLicenseData(
				Collections.singleton(ContentId.getContentId("npm/npmjs/-/write/0.2.0")), data -> results.add(data));
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
				"        \"declared\": \"EPL-2.0\", \n" +
				"        \"facets\": {\n" +
				"            \"core\": {\n" +
				"                \"discovered\": {\n" +
				"                    \"expressions\": [\n" +
				"                        \"EPL-2.0\"\n" +
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

		assertTrue(((ClearlyDefinedSupport) context.getClearlyDefinedService()).isAccepted(data));

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

		assertFalse(((ClearlyDefinedSupport) context.getClearlyDefinedService()).isAccepted(data));

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

		assertFalse(((ClearlyDefinedSupport) context.getClearlyDefinedService()).isAccepted(data));
	}
}
