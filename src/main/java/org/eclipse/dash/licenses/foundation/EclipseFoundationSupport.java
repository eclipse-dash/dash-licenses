/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.foundation;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.ISettings;

import com.google.common.flogger.FluentLogger;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public class EclipseFoundationSupport implements ILicenseDataProvider {
	private static final FluentLogger log = FluentLogger.forEnclosingClass();

	private ISettings settings;

	public EclipseFoundationSupport(ISettings settings) {
		this.settings = settings;
	}

	@Override
	public void queryLicenseData(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		if (ids.size() == 0)
			return;

		log.atInfo().log("Querying Eclipse Foundation for license data for %1$d items.", ids.size());

		String url = settings.getLicenseCheckUrl();

		try {
			JsonArrayBuilder builder = Json.createBuilderFactory(null).createArrayBuilder();
			ids.stream().forEach(id -> builder.add(id.toString()));
			String json = builder.build().toString();
			String form = URLEncoder.encode("json", StandardCharsets.UTF_8) + "="
					+ URLEncoder.encode(json, StandardCharsets.UTF_8);

			Duration timeout = Duration.ofSeconds(settings.getTimeout());
			HttpRequest request = HttpRequest.newBuilder(URI.create(url))
					.header("Content-Type", "application/x-www-form-urlencoded")
					.POST(BodyPublishers.ofString(form, StandardCharsets.UTF_8)).timeout(timeout).build();

			HttpClient httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				// FIXME Seems like overkill.
				AtomicInteger counter = new AtomicInteger();

				JsonReader reader = Json.createReader(new StringReader(response.body()));
				JsonObject read = (JsonObject) reader.read();

				JsonObject approved = read.getJsonObject("approved");
				if (approved != null)
					approved.forEach((key, each) -> {
						consumer.accept(new FoundationData(each.asJsonObject()));
						counter.incrementAndGet();
					});

				JsonObject restricted = read.getJsonObject("restricted");
				if (restricted != null)
					restricted.forEach((key, each) -> {
						consumer.accept(new FoundationData(each.asJsonObject()));
						counter.incrementAndGet();
					});

				log.atInfo().log("Found %1$d items.", counter.get());
			} else {
				log.atSevere().log("Eclipse Foundation data search time out; maybe decrease batch size.");
			}
		} catch (IOException | InterruptedException e) {
			// FIXME Handle gracefully
			throw new RuntimeException(e);
		}
	}

}
