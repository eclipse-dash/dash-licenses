/*************************************************************************
 * Copyright (c) 2019,2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dash.licenses.context.LicenseToolContext;
import org.eclipse.dash.licenses.spdx.SpdxExpression;
import org.eclipse.dash.licenses.spdx.SpdxExpressionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public class LicenseSupport {

	final Logger logger = LoggerFactory.getLogger(LicenseSupport.class);

	private Map<String, String> approvedLicenses;

	private LicenseToolContext ctx;

	public enum Status {
		Approved, Restricted
	}

	public LicenseSupport(LicenseToolContext context) {
		this.ctx = context;

	}

	private Map<String, String> getApprovedLicenses() {
		if (approvedLicenses == null) {

			ctx.getHttpClientService().get(ctx.getSettings().getApprovedLicensesUrl(), "application/json", response -> {
				approvedLicenses = readApprovedLicenses(new InputStreamReader(response));
			});
		}
		return approvedLicenses;
	}

	private Map<String, String> readApprovedLicenses(Reader contentReader) {
		JsonReader reader = Json.createReader(contentReader);
		JsonObject read = (JsonObject) reader.read();

		Map<String, String> licenses = new HashMap<>();
		JsonObject approved = read.getJsonObject("approved");
		if (approved != null) {
			approved.forEach((key, name) -> {
				logger.debug("Approved License {}", key);
				licenses.put(key.toUpperCase(), name.toString());
			});
		}

		return licenses;
	}

	/**
	 * Answer the compatibility status of an SPDX expression with the approved
	 * licenses list. The expression could be the id of a single license (e.g.,
	 * "epl-1.0" or "apache-2.0"), or an expression (e.g., "epl-1.0 or apache-2.0").
	 * Matches are case insensitive.
	 * <p>
	 * The expression is considered "approved" if it is either a single license on
	 * the approved licenses list, or--in the case of a license expression--the
	 * expression matches entries in the approved licenses list in a valid
	 * configuration.
	 *
	 * @param expression an SPDX expression.
	 * @return <code>Status.Approved</code> when the expression is approve, or
	 *         <code>Status.Restricted</code> otherwise
	 */
	public Status getStatus(String expression) {
		if (expression == null || expression.trim().isEmpty())
			return Status.Restricted;

		return getStatus(new SpdxExpressionParser().parse(expression));
	}

	public Status getStatus(SpdxExpression expression) {
		if (expression == null)
			return Status.Restricted;

		if (expression.matchesApproved(getApprovedLicenses().keySet())) {
			return Status.Approved;
		}

		return Status.Restricted;
	}
}
