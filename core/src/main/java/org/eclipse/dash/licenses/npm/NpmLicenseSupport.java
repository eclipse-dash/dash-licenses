/*************************************************************************
 * Copyright (c) 2024 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.npm;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * A fallback {@link ILicenseDataProvider} that queries the npm registry for
 * license information about JavaScript (npm) content.
 *
 * <p>
 * This provider is deliberately given a low weight so that it is only consulted
 * for content that the heavier providers (the Eclipse Foundation and
 * ClearlyDefined) were unable to approve. See {@link #getWeight()}.
 *
 * <p>
 * Only content of type <code>npm</code> from the <code>npmjs</code> source is
 * considered; everything else is ignored.
 *
 * <p>
 * The npm registry serves a document per package version at
 * <code>&lt;registry&gt;/&lt;name&gt;/&lt;version&gt;</code>. That document
 * exposes license information in one of two shapes:
 * <ul>
 * <li>a modern top-level <code>"license"</code> string containing an SPDX
 * expression (e.g. <code>"MIT"</code>), or</li>
 * <li>a legacy top-level <code>"licenses"</code> array of objects, each with a
 * <code>"type"</code> field.</li>
 * </ul>
 */
public class NpmLicenseSupport implements ILicenseDataProvider {

	final Logger logger = LoggerFactory.getLogger(NpmLicenseSupport.class);

	@Inject
	ISettings settings;
	@Inject
	IHttpClientService httpClientService;
	@Inject
	LicenseSupport licenseService;

	/**
	 * Lower than both the Eclipse Foundation (100) and ClearlyDefined (50)
	 * providers so that the npm registry is only queried for content that those
	 * heavier providers did not already approve.
	 */
	@Override
	public int getWeight() {
		return 10;
	}

	@Override
	public void queryLicenseData(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		String registry = settings.getNpmRegistryUrl();
		if (registry == null || registry.isBlank() || "skip".equals(registry)) {
			logger.debug("Bypassing npm registry.");
			return;
		}

		var npmIds = ids.stream().filter(this::isSupported).collect(Collectors.toList());
		if (npmIds.isEmpty())
			return;

		logger.info("Querying the npm registry for license data for {} items.", npmIds.size());

		// The npm registry exposes one document per package version, so (unlike the
		// batch-oriented providers) we query each id individually.
		npmIds.forEach(id -> query(registry, id, consumer));
	}

	private void query(String registry, IContentId id, Consumer<IContentData> consumer) {
		String url = toRegistryUrl(registry, id);
		if (url == null)
			return;

		int code = httpClientService.get(url, "application/json", response -> {
			try {
				String license = extractLicense(JsonUtils.readJson(response));
				if (license == null || license.isBlank()) {
					logger.debug("npm registry has no license information for {}", id);
					return;
				}

				var data = new NpmRegistryContentData(id, license, url);
				data.setStatus(licenseService.getStatus(license) == Status.Approved ? Status.Approved
						: Status.Restricted);
				consumer.accept(data);

				logger.debug("npm {} license: {} {}", id, license,
						data.getStatus() == Status.Approved ? "approved" : "restricted");
			} catch (RuntimeException e) {
				// A malformed response for a single package should not abort the whole
				// run; treat it as "no information found" and move on.
				logger.debug("Could not parse the npm registry response for {}: {}", id, e.getMessage());
			}
		});

		if (code != 200 && code != 404) {
			logger.debug("Unexpected response from the npm registry for {}: HTTP {}", id, code);
		}
	}

	/**
	 * Answer whether the given id is npm (JavaScript) content served by the npmjs
	 * registry.
	 */
	private boolean isSupported(IContentId id) {
		return "npm".equals(id.getType()) && "npmjs".equals(id.getSource());
	}

	/**
	 * Build the npm registry URL for a specific package version from an
	 * {@link IContentId}. Unscoped packages use a <code>"-"</code> namespace;
	 * scoped packages carry an <code>@scope</code> namespace that must be
	 * reassembled as <code>@scope/name</code> (url-encoded).
	 */
	String toRegistryUrl(String registry, IContentId id) {
		String name = id.getName();
		String version = id.getVersion();
		if (name == null || name.isBlank() || version == null || version.isBlank())
			return null;

		String base = registry.endsWith("/") ? registry.substring(0, registry.length() - 1) : registry;

		String namespace = id.getNamespace();
		String packageName;
		if (namespace == null || namespace.isBlank() || "-".equals(namespace)) {
			packageName = name;
		} else {
			// Scoped package: "@scope/name". The "/" within the scope must be encoded
			// so it is treated as a single path segment by the registry.
			String scope = namespace.startsWith("@") ? namespace : "@" + namespace;
			packageName = encode(scope + "/" + name);
		}

		return base + "/" + packageName + "/" + version;
	}

	private String encode(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			// UTF-8 is always supported.
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Extract an SPDX license expression from an npm version document, handling
	 * both the modern <code>"license"</code> string form and the legacy
	 * <code>"licenses"</code> array form.
	 */
	String extractLicense(JsonObject document) {
		// Modern form: a top-level "license" string.
		JsonValue license = document.get("license");
		if (license != null) {
			if (license.getValueType() == JsonValue.ValueType.STRING) {
				return ((JsonString) license).getString();
			}
			// Some very old packages used an object: { "type": "MIT", "url": ... }
			if (license.getValueType() == JsonValue.ValueType.OBJECT) {
				JsonString type = license.asJsonObject().getJsonString("type");
				if (type != null)
					return type.getString();
			}
		}

		// Legacy form: a top-level "licenses" array of { "type": ... } objects.
		JsonValue licenses = document.get("licenses");
		if (licenses != null && licenses.getValueType() == JsonValue.ValueType.ARRAY) {
			JsonArray array = licenses.asJsonArray();
			var types = array.stream().filter(each -> each.getValueType() == JsonValue.ValueType.OBJECT)
					.map(each -> each.asJsonObject().getJsonString("type")).filter(type -> type != null)
					.map(JsonString::getString).filter(type -> !type.isBlank()).collect(Collectors.toList());
			if (!types.isEmpty()) {
				// Multiple declared licenses are combined into a disjunctive SPDX
				// expression, matching how dual-licensed packages are interpreted.
				return types.size() == 1 ? types.get(0) : "(" + String.join(" OR ", types) + ")";
			}
		}

		return null;
	}
}
