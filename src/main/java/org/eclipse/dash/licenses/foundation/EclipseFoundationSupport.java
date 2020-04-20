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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.ISettings;

public class EclipseFoundationSupport implements ILicenseDataProvider {

	private ISettings settings;

	public EclipseFoundationSupport(ISettings settings) {
		this.settings = settings;
	}

	@Override
	public void queryLicenseData(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		if (ids.size() == 0)
			return;

		// FIXME Use proper logging
		System.out.println(String.format("Querying Eclipse Foundation for license data for %1$d items.", ids.size()));

		String url = settings.getLicenseCheckUrl();
		int timeout = settings.getTimeout();

		// @formatter:off
		RequestConfig config = RequestConfig.custom()
			.setConnectTimeout(timeout * 1000)
			.setConnectionRequestTimeout(timeout * 1000)
			.setSocketTimeout(timeout * 1000)
			.build();

		CloseableHttpClient httpclient = HttpClientBuilder.create()
			.setDefaultRequestConfig(config)
			.build();
		// @formatter:on

		try {
			JsonArrayBuilder builder = Json.createBuilderFactory(null).createArrayBuilder();
			ids.stream().forEach(id -> builder.add(id.toString()));
			String json = builder.build().toString();

			HttpPost post = new HttpPost(url);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("json", json));

			post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));

			CloseableHttpResponse response = httpclient.execute(post);
			if (response.getStatusLine().getStatusCode() == 200) {
				// FIXME Seems like overkill.
				AtomicInteger counter = new AtomicInteger();

				InputStream content = response.getEntity().getContent();
				JsonReader reader = Json.createReader(new InputStreamReader(content, "UTF-8"));
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

				content.close();
				// FIXME Use proper logging
				System.out.println(String.format("Found %1$d items.", counter.get()));
			} else {
				// FIXME Use proper logging
				System.out.println("Eclipse Foundation data search time out; maybe decrease batch size.");
			}
			response.close();
		} catch (IOException e) {
			// FIXME Handle gracefully
			throw new RuntimeException(e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
