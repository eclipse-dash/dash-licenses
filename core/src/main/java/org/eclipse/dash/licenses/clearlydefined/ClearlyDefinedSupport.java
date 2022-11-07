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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

	private Set<String> validTypes;
	private Set<String> validProviders;

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

		queryClearlyDefined(filteredIds, 0, filteredIds.size(), consumer);
	}

	/**
	 * This method coordinates calling ClearlyDefined.
	 * 
	 * We've run into cases where the ClearlyDefined API throws an error because of
	 * one apparently well-formed and otherwise correct ID. When this situation is
	 * encountered, an error message and nothing else is returned, regardless of how
	 * many IDs were included in the request. Effectively, this throws away all of
	 * the potentially useful results because of one (or more) problematic IDs.
	 * 
	 * When this happens, we split the content and attempt to invoke the API with
	 * each half. This happens recursively, so eventually we end up sending just the
	 * problematic IDs. We log the problematic IDs, but don't take any further
	 * action. IDs with problematic results are effectively be treated as IDs for
	 * which no information is found.
	 */
	private void queryClearlyDefined(List<IContentId> filteredIds, int start, int end,
			Consumer<IContentData> consumer) {
		try {
			doQueryClearlyDefined(filteredIds, start, end, consumer);
		} catch (ClearlyDefinedResponseException e) {
			if (start + 1 == end) {
				logger.info("Error querying ClearlyDefined for {}", filteredIds.get(start));
			} else {
				int middle = start + (end - start) / 2;
				queryClearlyDefined(filteredIds, start, middle, consumer);
				queryClearlyDefined(filteredIds, middle, end, consumer);
			}
		}
	}

	private void doQueryClearlyDefined(List<IContentId> ids, int start, int end, Consumer<IContentData> consumer) {
		// If there's nothing to do, bail out.
		if (start == end)
			return;

		int code = httpClientService
				.post(settings.getClearlyDefinedDefinitionsUrl(), "application/json",
						JsonUtils.toJson(ids.subList(start, end)), response -> {
							// FIXME Seems like overkill.
							AtomicInteger counter = new AtomicInteger();

							try {
								JsonUtils.readJson(new StringReader(response)).forEach((key, each) -> {
									ClearlyDefinedContentData data = new ClearlyDefinedContentData(key,
											each.asJsonObject());
									data.setStatus(isAccepted(data) ? Status.Approved : Status.Restricted);
									consumer.accept(data);
									counter.incrementAndGet();
									logger
											.debug("ClearlyDefined {} score: {} {} {}", data.getId(), data.getScore(),
													data.getLicense(),
													data.getStatus() == Status.Approved ? "approved" : "restricted");
								});

								logger.info("Found {} items.", counter.get());
							} catch (JsonParsingException e) {
								logger.error("Could not parse the response from ClearlyDefined: {}.", response);
								logger.debug(e.getMessage(), e);
								throw new ClearlyDefinedResponseException(e);
							}
						});

		if (code == 500) {
			logger.error("A server error occurred while contacting ClearlyDefined");
			throw new ClearlyDefinedResponseException();
		}

		if (code != 200) {
			logger.error("Error response from ClearlyDefined {}", code);
			throw new RuntimeException("Received an error response from ClearlyDefined.");
		}
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
			return !data
					.discoveredLicenses()
					.filter(license -> !"NONE".equals(license))
					.filter(license -> !isDiscoveredLicenseApproved(license))
					.findAny()
					.isPresent();
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
		 * 
		 * FIXME A hack on top of a hack. I suspect that we're hitting a rate limit on
		 * raw.githubusercontent.com. We need a better solution that what we have and
		 * the hack to grab the information from the project's GitHub repository doesn't
		 * appear to cut it. In the meantime, I'm just hardcoding the acceptable values.
		 */
		validTypes = new HashSet<>();
		validProviders = new HashSet<>();

		validTypes
				.addAll(Arrays.asList(new String[]
				{ "npm", "crate", "git", "maven", "nuget", "gem", "go", "composer", "pod", "pypi", "sourcearchive",
						"deb", "debsrc" }));

		validProviders
				.addAll(Arrays.asList(new String[]
				{ "npmjs", "cocoapods", "cratesio", "github", "gitlab", "mavencentral", "mavengoogle", "gradleplugin",
						"packagist", "golang", "nuget", "rubygems", "pypi", "debian" }));
	}

	class ClearlyDefinedResponseException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public ClearlyDefinedResponseException(Exception e) {
			super(e);
		}

		public ClearlyDefinedResponseException() {
			super();
		}
	}
}
