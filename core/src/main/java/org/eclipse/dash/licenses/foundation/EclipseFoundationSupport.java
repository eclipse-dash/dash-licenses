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

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.context.IContext;

import com.google.common.flogger.FluentLogger;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public class EclipseFoundationSupport implements ILicenseDataProvider {
	private IContext context;
	private static final FluentLogger log = FluentLogger.forEnclosingClass();

	public EclipseFoundationSupport(IContext context) {
		this.context = context;
	}

	@Override
	public void queryLicenseData(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		if (ids.size() == 0)
			return;

		log.atInfo().log("Querying Eclipse Foundation for license data for %1$d items.", ids.size());

		String url = context.getSettings().getLicenseCheckUrl();

		JsonArrayBuilder builder = Json.createBuilderFactory(null).createArrayBuilder();
		ids.stream().forEach(id -> builder.add(id.toString()));
		String json = builder.build().toString();
		String form = URLEncoder.encode("json", StandardCharsets.UTF_8) + "="
				+ URLEncoder.encode(json, StandardCharsets.UTF_8);

		int code = context.getHttpClientService().post(url, "application/x-www-form-urlencoded", form, response -> {
			AtomicInteger counter = new AtomicInteger();

			JsonReader reader = Json.createReader(new StringReader(response));
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
		});
		if (code != 200) {
			log.atSevere().log("Eclipse Foundation data search time out; maybe decrease batch size.");
		}
	}

}
