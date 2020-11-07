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
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.util.JsonUtils;

import com.google.common.flogger.FluentLogger;

public class ClearlyDefinedSupport implements ILicenseDataProvider {
	private static final FluentLogger log = FluentLogger.forEnclosingClass();

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
		/*
		 * Only ask ClearlyDefined for information about content that we know that it
		 * may actually have an answer for.
		 */
		List<IContentId> filteredIds = ids.stream().filter(id -> isSupported(id)).collect(Collectors.toList());

		if (filteredIds.size() == 0)
			return;

		log.atInfo().log("Querying ClearlyDefined for license data for %1$d items.", filteredIds.size());

		try {
			Duration timeout = Duration.ofSeconds(settings.getTimeout());
			HttpRequest request = HttpRequest.newBuilder(URI.create(settings.getClearlyDefinedDefinitionsUrl()))
					.header("Content-Type", "application/json")
					.POST(BodyPublishers.ofString(JsonUtils.toJson(filteredIds), StandardCharsets.UTF_8))
					.timeout(timeout).build();

			HttpClient httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				// FIXME Seems like overkill.
				AtomicInteger counter = new AtomicInteger();

				JsonUtils.readJson(new StringReader(response.body())).forEach((key, each) -> {
					ClearlyDefinedContentData data = new ClearlyDefinedContentData(key, each.asJsonObject());

					if (isAccepted(data)) {
						data.setStatus(LicenseSupport.Status.Approved);
						consumer.accept(data);
						counter.incrementAndGet();
					}
				});

				log.atInfo().log("Found %1$d items.", counter.get());
			} else {
				log.atSevere().log("ClearlyDefined data search time out; maybe decrease batch size.");
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Answers whether or not this id is supported by ClearlyDefined.
	 * 
	 * @param id
	 * @return
	 */
	private boolean isSupported(IContentId id) {
		/*
		 * HACK: ClearlyDefined throws an error when we send it types or sources that it
		 * doesn't recognise. So let's avoid doing that. Since we don't have a means of
		 * knowing what types and sources are supported, we take an approach of
		 * identifying those items that we know aren't supported.
		 */
		return !"p2".equals(id.getType());
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
			return !data.discoveredLicenses().filter(license -> !"NONE".equals(license))
					.filter(license -> !isDiscoveredLicenseApproved(license)).findAny().isPresent();
		}
		return false;
	}

	boolean isDiscoveredLicenseApproved(String license) {
		return licenseSupport.getStatus(license) == LicenseSupport.Status.Approved;
	}

}
