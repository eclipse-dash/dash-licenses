/*************************************************************************
 * Copyright (c) 2021, 2022 The Eclipse Foundation and others.
 * Copyright (c) 2021 Oleksandr Andriienko
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.dash.licenses.context.LicenseToolContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientService implements IHttpClientService {
	final Logger logger = LoggerFactory.getLogger(HttpClientService.class);

	final static int MAX_TRIES = 10;

	private LicenseToolContext ctx;

	public HttpClientService(LicenseToolContext context) {
		this.ctx = context;
	}

	@Override
	public int post(String url, String contentType, String payload, Consumer<String> handler) {
		try {
			var tries = 0;
			while (true) {
				Duration timeout = Duration.ofSeconds(ctx.getSettings().getTimeout());
				HttpRequest request = HttpRequest
						.newBuilder(URI.create(url))
						.header("Content-Type", contentType)
						.POST(BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
						.timeout(timeout)
						.build();

				HttpClient httpClient = getHttpClient(timeout);
				HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
				if (response.statusCode() == 502 && tries++ < MAX_TRIES) {
					logger.info("HTTP response 502 (Bad Gateway). Trying again...");
					Thread.sleep(1000 * tries);
					continue;
				}
				if (response.statusCode() == 200) {
					handler.accept(response.body());
				}
				return response.statusCode();
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean remoteFileExists(String url) {
		try {
			HttpRequest.Builder reqBuilder = HttpRequest
					.newBuilder(URI.create(url))
					.method("HEAD", HttpRequest.BodyPublishers.noBody());

			Duration timeout = Duration.ofSeconds(ctx.getSettings().getTimeout());
			HttpRequest request = reqBuilder.timeout(timeout).build();

			HttpClient httpClient = getHttpClient(timeout);
			HttpResponse<Void> response = httpClient.send(request, BodyHandlers.discarding());
			return (response.statusCode() < HttpURLConnection.HTTP_BAD_REQUEST);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public int get(String url, String contentType, Consumer<InputStream> handler) {
		return get(url, contentType, Collections.emptyMap(), handler);
	}

	@Override
	public int get(String url, String contentType, Map<String, String> headers, Consumer<InputStream> handler) {
		try {
			var tries = 0;
			while (true) {
				HttpRequest.Builder reqBuilder = HttpRequest
						.newBuilder(URI.create(url))
						.header("Content-Type", contentType)
						.GET();

				headers.forEach((key, value) -> reqBuilder.header(key, value));

				Duration timeout = Duration.ofSeconds(ctx.getSettings().getTimeout());
				HttpRequest request = reqBuilder.timeout(timeout).build();

				HttpClient httpClient = getHttpClient(timeout);

				HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
				if (response.statusCode() == 502 && tries++ < MAX_TRIES) {
					logger.info("HTTP response 502 (Bad Gateway). Trying again...");
					Thread.sleep(1000 * tries);
					continue;
				}
				if (response.statusCode() == 200) {
					handler.accept(response.body());
				}
				return response.statusCode();
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	protected HttpClient getHttpClient(Duration timeout) {
		HttpClient.Builder builder = HttpClient
				.newBuilder()
				.connectTimeout(timeout)
				.followRedirects(HttpClient.Redirect.ALWAYS);

		// Configure proxy, if any
		ctx.proxySettings().ifPresent(proxySettings -> proxySettings.configure(builder));

		return builder.build();
	}
}
