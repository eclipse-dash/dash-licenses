/***********************************************************************
 * Copyright (c) 2020,2024 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.review;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import java.util.function.Function;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public class GitLabApi implements AutoCloseable {

	private static final int STATUS_OK = 200;
	private static final String PRIVATE_TOKEN_HEADER = "PRIVATE-TOKEN";

	private HttpClient client;
	private String ipLabHostUrl;
	private String ipLabToken;

	public GitLabApi(String ipLabHostUrl, String ipLabToken, HttpClient.Builder builder) {
		this.ipLabHostUrl = ipLabHostUrl;
		this.ipLabToken = ipLabToken;
		this.client = builder.build();
	}

	@Override
	public void close() {
		client.close();
	}

	public Optional<GitLabIssue> getAnyIssueOpenWithTitle(String projectId, String title) throws GitLabApiException {

		HttpRequest request = HttpRequest
				.newBuilder()
				.header(PRIVATE_TOKEN_HEADER, ipLabToken)
				.uri(URI
						.create(ipLabHostUrl)
						.resolve("projects")
						.resolve(projectId)
						.resolve("issues?in=title&search=" + title))
				.GET()
				.build();

		try {
			HttpResponse<InputStream> res = client.send(request, BodyHandlers.ofInputStream());
			if (res.statusCode() == STATUS_OK) {
				JsonReader reader = Json.createReader(res.body());
				JsonArray array = reader.readArray();

				return array
						.stream()
						.filter(JsonObject.class::isInstance)
						.map(JsonObject.class::cast)
						.filter(o -> title.equals(o.getString("title", null)))
						.findAny()
						.map(GitLabApi.toIssue());

			} else {

				throw new GitLabApiException("Bad Status Code", res.statusCode());
			}
		} catch (IOException | InterruptedException e) {
			throw new GitLabApiException(e.getMessage());
		}
	}

	public GitLabIssue createIssue(String projectId, String title, String description, String labels)
			throws GitLabApiException {

		HttpRequest request = HttpRequest
				.newBuilder()
				.header(PRIVATE_TOKEN_HEADER, ipLabToken)
				.uri(URI
						.create(ipLabHostUrl)
						.resolve("projects")
						.resolve(projectId)
						.resolve("issues?title=" + title + "&labels=" + title + "&description=" + description))
				.POST(BodyPublishers.noBody())
				.build();

		try {
			HttpResponse<InputStream> res = client.send(request, BodyHandlers.ofInputStream());
			if (res.statusCode() == STATUS_OK) {

				JsonReader reader = Json.createReader(res.body());
				JsonObject jsonObject = reader.readObject();

				return GitLabApi.toIssue().apply(jsonObject);

			} else {

				throw new GitLabApiException("Bad Status Code", res.statusCode());
			}
		} catch (IOException | InterruptedException e) {
			throw new GitLabApiException(e.getMessage());
		}
	}

	public GitLabUser getCurrentUser() throws GitLabApiException {
		HttpRequest request = HttpRequest
				.newBuilder()
				.header(PRIVATE_TOKEN_HEADER, ipLabToken)
				.uri(URI.create(ipLabHostUrl).resolve("user"))
				.GET()
				.build();

		try {
			HttpResponse<InputStream> res = client.send(request, BodyHandlers.ofInputStream());
			if (res.statusCode() == STATUS_OK) {
				JsonReader reader = Json.createReader(res.body());
				JsonObject jsonObject = reader.readObject();

				return GitLabApi.toUser().apply(jsonObject);

			} else {

				throw new GitLabApiException("Bad Status Code", res.statusCode());
			}
		} catch (IOException | InterruptedException e) {
			throw new GitLabApiException(e.getMessage());
		}
	}

	private static Function<JsonObject, GitLabIssue> toIssue() {
		return json -> new GitLabIssue().withTitle(json.getString("title")).withWebUrl(json.getString("web_url"));
	}

	private static Function<JsonObject, GitLabUser> toUser() {
		return json -> new GitLabUser().withUsername(json.getString("username"));
	}
}
