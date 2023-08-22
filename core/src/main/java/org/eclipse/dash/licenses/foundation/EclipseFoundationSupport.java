/*************************************************************************
 * Copyright (c) 2019,2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.foundation;

import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import jakarta.inject.Inject;

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

public class EclipseFoundationSupport implements ILicenseDataProvider {
	@Inject
	ISettings settings;
	@Inject
	IHttpClientService httpClientService;

	final Logger logger = LoggerFactory.getLogger(EclipseFoundationSupport.class);

	@Override
	public void queryLicenseData(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		if (ids.isEmpty())
			return;

		String url = settings.getLicenseCheckUrl();
		if (url.isBlank()) {
			logger.debug("Bypassing Eclipse Foundation.");
			return;
		}

		logger.info("Querying Eclipse Foundation for license data for {} items.", ids.size());

		String form = encodeRequestPayload(ids);

		int code = httpClientService.post(url, "application/x-www-form-urlencoded", form, response -> {
			AtomicInteger counter = new AtomicInteger();

			JsonReader reader = Json.createReader(new StringReader(response));
			JsonObject read = (JsonObject) reader.read();

			JsonObject approved = read.getJsonObject("approved");
			if (approved != null)
				approved.forEach((key, each) -> {
					FoundationData data = new FoundationData(each.asJsonObject());
					logger.debug("EF approved: {} ({}) score: {} {} {}", data.getId(), data.getRule(), data.getScore(),
							data.getLicense(), data.getAuthority());
					consumer.accept(data);
					counter.incrementAndGet();
				});

			JsonObject restricted = read.getJsonObject("restricted");
			if (restricted != null)
				restricted.forEach((key, each) -> {
					FoundationData data = new FoundationData(each.asJsonObject());
					logger.debug("EF restricted: {} score: {} {} {}", data.getId(), data.getScore(), data.getLicense(),
							data.getAuthority());
					consumer.accept(data);
					counter.incrementAndGet();
				});

			logger.info("Found {} items.", counter.get());
		});
		if (code != 200) {
			logger.error("Error response from the Eclipse Foundation {}", code);
			throw new RuntimeException("Received an error response from the Eclipse Foundation.");
		}
	}

	private String encodeRequestPayload(Collection<IContentId> ids) {
		JsonObject build = buildRequestPayload(ids);
		String json = build.toString();
		String form = URLEncoder.encode("request", StandardCharsets.UTF_8) + "="
				+ URLEncoder.encode(json, StandardCharsets.UTF_8);
		return form;
	}

	private JsonObject buildRequestPayload(Collection<IContentId> ids) {
		JsonObjectBuilder request = Json.createObjectBuilder();

		var projectId = settings.getProjectId();
		if (projectId != null) {
			logger.debug("Querying for project {}.", projectId);
			request.add("project", projectId);
		}

		JsonArrayBuilder builder = Json.createBuilderFactory(null).createArrayBuilder();
		ids.stream().forEach(id -> {
			builder.add(id.toString());
			logger.debug("Sending to EF {}", id);
		});

		request.add("dependencies", builder);

		return request.build();
	}

	private String encodeJsonPayload(Collection<IContentId> ids) {
		JsonArray build = buildJsonPayload(ids);
		String json = build.toString();
		String form = URLEncoder.encode("json", StandardCharsets.UTF_8) + "="
				+ URLEncoder.encode(json, StandardCharsets.UTF_8);
		return form;
	}

	private JsonArray buildJsonPayload(Collection<IContentId> ids) {
		JsonArrayBuilder builder = Json.createBuilderFactory(null).createArrayBuilder();
		ids.stream().forEach(id -> builder.add(id.toString()));

		return builder.build();
	}

}
