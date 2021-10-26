/*************************************************************************
 * Copyright (c) 2019,2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.clearlydefined;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.LicenseSupport.Status;
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.eclipse.dash.licenses.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.stream.JsonParsingException;

public class ClearlyDefinedSupport implements ILicenseDataProvider {
	final Logger logger = LoggerFactory.getLogger(ClearlyDefinedSupport.class);

	@Inject
	ISettings settings;
	@Inject
	IHttpClientService httpClientService;
	@Inject
	LicenseSupport licenseService;

	private HashSet<String> validTypes;
	private HashSet<String> validProviders;

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

		if (filteredIds.isEmpty())
			return;

		logger.info("Querying ClearlyDefined for license data for {} items.", filteredIds.size());

		if (logger.isDebugEnabled()) {
			filteredIds.forEach(each -> logger.debug("Sending: {}", each));
		}

		int code = httpClientService.post(settings.getClearlyDefinedDefinitionsUrl(), "application/json",
				JsonUtils.toJson(filteredIds), response -> {
					// FIXME Seems like overkill.
					AtomicInteger counter = new AtomicInteger();

					try {
						JsonUtils.readJson(new StringReader(response)).forEach((key, each) -> {
							ClearlyDefinedContentData data = new ClearlyDefinedContentData(key, each.asJsonObject());
							data.setStatus(isAccepted(data) ? Status.Approved : Status.Restricted);
							consumer.accept(data);
							counter.incrementAndGet();
							logger.debug("ClearlyDefined {} score: {} {} {}", data.getId(), data.getScore(),
									data.getLicense(), data.getStatus() == Status.Approved ? "approved" : "restricted");
						});

						logger.info("Found {} items.", counter.get());
					} catch (JsonParsingException e) {
						logger.error("Could not parse the response from ClearlyDefined: {}.", response);
						logger.debug(e.getMessage(), e);
						throw new RuntimeException("Could not parse the response from ClearlyDefined.");
					}
				});

		if (code != 200)
			logger.error("ClearlyDefined data search time out; maybe decrease batch size.");
	}

	/**
	 * Answers whether or not this id is supported by ClearlyDefined.
	 * 
	 * @param id
	 * @return
	 */
	private boolean isSupported(IContentId id) {
		if (!validTypes.contains(id.getType()))
			return false;
		if (!validProviders.contains(id.getSource()))
			return false;
		return true;
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
		if (data.getLicenseScore() >= settings.getConfidenceThreshold()) {
			if (licenseService.getStatus(data.getLicense()) != LicenseSupport.Status.Approved)
				return false;
			return !data.discoveredLicenses().filter(license -> !"NONE".equals(license))
					.filter(license -> !isDiscoveredLicenseApproved(license)).findAny().isPresent();
		}
		return false;
	}

	boolean isDiscoveredLicenseApproved(String license) {
		return licenseService.getStatus(license) == LicenseSupport.Status.Approved;
	}

	@Inject
	void bootstrap() {
		/*
		 * FIXME This is a hack. AFAICT, there is no API that answers the list of valid
		 * types and providers, so we grab them directly from a schema file in the
		 * GitHub repository. This is not an official API and so is subject to change.
		 */
		var validTypes = new HashSet<String>();
		var validProviders = new HashSet<String>();

		var code = httpClientService.get(
				"https://raw.githubusercontent.com/clearlydefined/service/HEAD/schemas/curation-1.0.json",
				"application/json", response -> {
					try {
						var data = JsonUtils.readJson(response);
						var definitions = data.asJsonObject().getJsonObject("definitions");

						var types = definitions.getJsonObject("type").getJsonArray("enum");
						for (int index = 0; index < types.size(); index++) {
							validTypes.add(types.getString(index));
						}

						var providers = definitions.getJsonObject("provider").getJsonArray("enum");
						for (int index = 0; index < providers.size(); index++) {
							validProviders.add(providers.getString(index));
						}
					} catch (RuntimeException e) {
						throw new RuntimeException(
								"Invalid data received while bootstrapping the ClearlyDefined Service.", e);
					}
				});
		if (code != 200) {
			throw new RuntimeException("Cannot acquire data required to bootstrap the ClearlyDefined Service");
		}
		this.validTypes = validTypes;
		this.validProviders = validProviders;
	}

}
