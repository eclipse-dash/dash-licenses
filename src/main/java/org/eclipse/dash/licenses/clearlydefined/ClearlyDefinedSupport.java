/*************************************************************************
 * Copyright (c) 2019,2020 The Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.clearlydefined;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.util.JsonUtils;

public class ClearlyDefinedSupport implements ILicenseDataProvider {

	private ISettings settings;
	// TODO Obvious opportunity for dependency injection
	private LicenseSupport licenseSupport;

	public ClearlyDefinedSupport(ISettings settings) {
		this.settings = settings;
		licenseSupport = LicenseSupport.getLicenseSupport(settings);
	}

	/**
	 * The ClearlyDefined API expects a flat array of ids in JSON format in the
	 * payload of the POST request.
	 * 
	 * <pre>
	 * {
	 *	"maven/mavencentral/io.netty/netty-transport/4.1.42",
	 *	"maven/mavencentral/io.netty/netty-resolver/4.1.42",
	 * 	...
	 * }
	 * </pre>
	 * 
	 * And answers an associative array in JSON format similar to the following.
	 * 
	 * <pre>
	 * {
	 *	"maven/mavencentral/io.netty/netty-transport/4.1.42":{ ... },
	 *	"maven/mavencentral/io.netty/netty-resolver/4.1.42":{ ... },
	 *	...
	 * }
	 * </pre>
	 * 
	 * See the {@link ClearlyDefinedSupport} type for an example of the value.
	 * 
	 * Note that the ClearlyDefined API will always return a value for every id that
	 * is provided. In cases where the id is not in the ClearlyDefined database, the
	 * score is reported as 0. This implementation will only pass those values that
	 * have a score greater than the confidence threshold (as specified by the
	 * settings); other values are ignored.
	 * 
	 * @param ids      ids to search in five-part ClearlyDefined format.
	 * @param consumer the closure to execute with a instance of
	 *                 {@link ClearlyDefinedContentData} for each value included in
	 *                 the result.
	 */
	@Override
	public void queryLicenseData(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		if (ids.size() == 0)
			return;

		// FIXME Use proper logging
		System.out.println(String.format("Querying ClearlyDefined for license data for %1$d items.", ids.size()));

		try (CloseableHttpClient httpclient = getHttpClient()) {
			HttpPost post = new HttpPost(settings.getClearlyDefinedDefinitionsUrl());
			post.setEntity(new StringEntity(JsonUtils.toJson(ids), ContentType.APPLICATION_JSON));

			try (CloseableHttpResponse response = httpclient.execute(post)) {
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// FIXME Seems like overkill.
					AtomicInteger counter = new AtomicInteger();

					try (InputStream content = response.getEntity().getContent()) {

						JsonUtils.readJson(content).forEach((key, each) -> {
							ClearlyDefinedContentData data = new ClearlyDefinedContentData(key, each.asJsonObject());

							if (isAccepted(data)) {
								data.setStatus(LicenseSupport.Status.Approved);
								consumer.accept(data);
								counter.incrementAndGet();
							} else {
								// FIXME Use proper logging
								System.out.println(String.format("Rejected: %1$s", data.getUrl()));
							}
						});
					}

					// FIXME Use proper logging
					System.out.println(String.format("Found %1$d items.", counter.get()));
				} else {
					// FIXME Use proper logging
					System.out.println("ClearlyDefined data search time out; maybe decrease batch size.");
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Answers whether or not an entry retrieved from ClearlyDefined should be
	 * accepted. This is <code>true</code> when the score is not below the threshold
	 * (as defined by the settings) and all of the discovered licenses are in the
	 * Eclipse Foundation acceptable licenses list. Answers <code>false</code>
	 * otherwise.
	 * 
	 * @see LicenseSupport
	 * @see ClearlyDefinedContentData
	 * 
	 * @param data An instance of {@link ClearlyDefinedContentData} representing one
	 *             row from the results.
	 * @return <code>true</code> when the data is acceptable, <code>false</code>
	 *         otherwise.
	 */
	public boolean isAccepted(ClearlyDefinedContentData data) {
		if (data.getEffectiveScore() >= settings.getConfidenceThreshold()
				|| data.getLicenseScore() >= settings.getConfidenceThreshold()) {
			if (licenseSupport.getStatus(data.getLicense()) != LicenseSupport.Status.Approved)
				return false;
			return !data.discoveredLicenses().filter(license -> !isDiscoveredLicenseApproved(license)).findAny()
					.isPresent();
		}
		return false;
	}

	boolean isDiscoveredLicenseApproved(String license) {
		return licenseSupport.getStatus(license) == LicenseSupport.Status.Approved;
	}

	CloseableHttpClient getHttpClient() {
		int timeout = settings.getTimeout() * 1000;

		// @formatter:off
		RequestConfig config = RequestConfig.custom()
			.setConnectTimeout(timeout)
			.setConnectionRequestTimeout(timeout)
			.setSocketTimeout(timeout)
			.build();
		// @formatter:on

		return HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	}
}
