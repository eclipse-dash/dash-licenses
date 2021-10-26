/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.extended;

import java.util.function.Consumer;

import javax.inject.Inject;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.eclipse.dash.licenses.util.JsonUtils;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class PypiExtendedContentDataProvider implements IExtendedContentDataProvider {
	@Inject
	ISettings settings;
	@Inject
	IHttpClientService httpClientService;

	@Override
	public ExtendedContentData getExtendedContentData(IContentId id) {
		if (!"pypi".equals(id.getType()))
			return null;
		if (!"pypi".equals(id.getSource()))
			return null;

		var builder = new PypiPackageBuilder(id);

		getMetadata(id, data -> {
			builder.setMetadata(data);
		});

		return builder.build();
	}

	private void getMetadata(IContentId id, Consumer<JsonObject> consumer) {
		httpClientService.get(getMetadataUrl(id), "application/json", inputStream -> {
			consumer.accept(JsonUtils.readJson(inputStream));
		});
	}

	private String getMetadataUrl(IContentId id) {
		return translate("https://pypi.org/pypi/{name}/json", id);
	}

	private String translate(String pattern, IContentId id) {
		var url = pattern;
		url = url.replace("{namespace}", id.getNamespace().replace("%2F", "/"));
		url = url.replace("{name}", id.getName());
		url = url.replace("{revision}", id.getVersion());

		return url;
	}

	class PypiPackageBuilder {

		private IContentId id;
		private JsonObject metadata = JsonValue.EMPTY_JSON_OBJECT;

		public PypiPackageBuilder(IContentId id) {
			this.id = id;
		}

		public ExtendedContentData build() {
			var thing = new ExtendedContentData("pypi", getUrl());
			for (JsonValue each : getVersionMetadata()) {
				switch (each.asJsonObject().getString("packagetype")) {
				case "sdist":

//					if (httpClientService.remoteFileExists(url.toString()))
//						return url.toString();
					thing.addLink("Source", each.asJsonObject().getString("url"));
					break;
				default:
				}
			}

			return thing;
		}

		String getUrl() {
			return translate("https://pypi.org/project/{name}", id);
		}

		void setMetadata(JsonObject data) {
			this.metadata = data;
		}

		JsonArray getVersionMetadata() {
			return metadata.getOrDefault("releases", JsonValue.EMPTY_JSON_OBJECT).asJsonObject()
					.getOrDefault(id.getVersion(), JsonValue.EMPTY_JSON_ARRAY).asJsonArray();
		}
	}
}
