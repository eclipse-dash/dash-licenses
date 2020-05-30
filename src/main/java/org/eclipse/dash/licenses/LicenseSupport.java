/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class LicenseSupport {

	private Map<String, String> approvedLicenses;

	public enum Status {
		Approved, Restricted
	};

	private LicenseSupport(Map<String, String> approvedLicenses) {
		this.approvedLicenses = approvedLicenses;
	}

	public static LicenseSupport getLicenseSupport(ISettings settings) {
		Map<String, String> approvedLicenses = getApprovedLicenses(settings);
		return new LicenseSupport(approvedLicenses);
	}

	public static LicenseSupport getLicenseSupport(Reader reader) {
		Map<String, String> approvedLicenses = getApprovedLicenses(reader);
		return new LicenseSupport(approvedLicenses);
	}

	private static Map<String, String> getApprovedLicenses(ISettings settings) {

		String url = settings.getApprovedLicensesUrl();

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpGet get = new HttpGet(url);

			try (CloseableHttpResponse response = httpclient.execute(get)) {
				if (response.getStatusLine().getStatusCode() == 200) {
					try (InputStream content = response.getEntity().getContent();
							InputStreamReader contentReader = new InputStreamReader(content, StandardCharsets.UTF_8)) {
						return getApprovedLicenses(contentReader);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	private static Map<String, String> getApprovedLicenses(Reader contentReader) {
		JsonReader reader = Json.createReader(contentReader);
		JsonObject read = (JsonObject) reader.read();

		Map<String, String> licenses = new HashMap<>();
		read.getJsonObject("approved").forEach((key, name) -> licenses.put(key.toUpperCase(), name.toString()));

		// Augment the official list with licenses that are acceptable, but
		// not explicitly included in our approved list.
		licenses.put("EPL-1.0", "Eclipse Public License, v1.0");
		licenses.put("EPL-2.0", "Eclipse Public License, v2.0");
		licenses.put("WTFPL", "WTFPL");
		licenses.put("CC-BY-3.0", "CC-BY-3.0");
		licenses.put("CC-BY-4.0", "CC-BY-4.0");
		licenses.put("UNLICENSE", "Unlicense");
		licenses.put("ARTISTIC-2.0", "Artistic-2.0");
		licenses.put("BSD-2-Clause-FreeBSD", "BSD 2-Clause FreeBSD License");
		return licenses;
	}

	/**
	 * Answer the compatibility status of an SPDX expression with the license white
	 * list. The expression could be the id of a single license (e.g., "epl-1.0" or
	 * "apache-2.0"), or an expression (e.g., "epl-1.0 or apache-2.0"). Matches are
	 * case insensitive.
	 * <p>
	 * The expression is considered "approved" if it is either a single license on
	 * the white list, or if any of the license in a disjunctive expression. All
	 * others, including more complex expressions, are considered "restricted" and
	 * require review by the IP Team.
	 * 
	 * @param expression an SPDX expression.
	 * @return <code>Status.Approved</code> when the expression is approve, or
	 *         <code>Status.Restricted</code> otherwise
	 */
	public Status getStatus(String expression) {
		if (expression == null)
			return Status.Restricted;

		// TODO We possibly need more sophisticated expression parsing.

		// This is a quick and dirty "for now" solution. I'm reasoning that
		// an AND condition is weird and needs attention. Over time, we'll
		// probably get a better sense of what more we can automate.
		String spdx = expression.toUpperCase();
		if (spdx.contains("AND"))
			return Status.Restricted;

		// FIXME This will have some odd results
		// e.g., "GPL-2.0 OR EPL-2.0 or Apache-2.0" will work, but "GPL-2.0 OR (EPL-2.0
		// or Apache-2.0)"
		// will not.
		for (String id : spdx.split("\\s+OR\\s+")) {
			if (approvedLicenses.containsKey(id))
				return Status.Approved;
		}
		return Status.Restricted;
	}
}
