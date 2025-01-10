/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.dash.licenses.ILicenseDataProvider;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseChecker;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedSupport;
import org.eclipse.dash.licenses.foundation.EclipseFoundationSupport;
import org.eclipse.dash.licenses.http.IHttpClientService;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;

public class TestLicenseToolModule extends AbstractModule {

	private ISettings settings;

	public TestLicenseToolModule() {
		this.settings = new ISettings() {
		};
	}

	@Override
	protected void configure() {
		bind(ISettings.class).toInstance(settings);
		bind(IHttpClientService.class).toInstance(getHttpClientService());
		bind(LicenseChecker.class).toInstance(new LicenseChecker());
		bind(LicenseSupport.class).toInstance(new LicenseSupport());

		var licenseDataProviders = Multibinder.newSetBinder(binder(), ILicenseDataProvider.class);
		licenseDataProviders.addBinding().toInstance(new EclipseFoundationSupport() {
			@Override
			public int getWeight() {
				return 100;
			}
		});
		licenseDataProviders.addBinding().toInstance(new ClearlyDefinedSupport());
	}

	public IHttpClientService getHttpClientService() {
		return new IHttpClientService() {
			@Override
			public int post(String url, String contentType, String payload, Consumer<String> handler) {
				if (url.equals(settings.getClearlyDefinedDefinitionsUrl())) {
					// The file contains only the information for the one record; the
					// ClearlyDefined service expects a Json collection as the response,
					// so insert the file contents into an array and pass that value to
					// the handler.
					JsonReader reader = Json.createReader(new StringReader(payload));
					JsonArray items = (JsonArray) reader.read();

					var builder = new StringBuilder();
					builder.append("{");
					for (int index = 0; index < items.size(); index++) {
						if (index > 0)
							builder.append(",");
						var id = items.getString(index);
						builder.append("\"");
						builder.append(id);
						builder.append("\" :");

						switch (id) {
						case "npm/npmjs/-/write/1.0.3":
						case "npm/npmjs/-/write/1.0.4":
						case "npm/npmjs/-/write/1.0.5":
						case "npm/npmjs/-/write/1.0.6":
							appendFileContents(builder, "/write-1.0.3.json");
							break;
						case "npm/npmjs/@yarnpkg/lockfile/1.1.0":
						case "npm/npmjs/@yarnpkg/lockfile/1.1.1":
						case "npm/npmjs/@yarnpkg/lockfile/1.1.2":
						case "npm/npmjs/@yarnpkg/lockfile/1.1.3":
							appendFileContents(builder, "/lockfile-1.1.0.json");
							break;
						/*
						 * I've run into cases where the ClearlyDefined API throws an error because of
						 * one apparently well-formed and otherwise correct ID. When this situation is
						 * encountered, an error message and nothing else is returned, regardless of how
						 * many IDs were included in the request.
						 */
						case "npm/npmjs/breaky/mcbreakyface/1.0.0":
						case "npm/npmjs/breaky/mcbreakyface/1.0.1":
							handler
									.accept("An error occurred when trying to fetch coordinates for one of the components");
							return 200;
						default:
							builder.append("{}");
						}
					}
					builder.append("}");

					handler.accept(builder.toString());

					return 200;
				}

				if (url.equals(settings.getLicenseCheckUrl())) {

					if (payload.startsWith("request=")) {
						var json = URLDecoder.decode(payload.substring("request=".length()), StandardCharsets.UTF_8);

						var approved = Json.createObjectBuilder();
						var rejected = Json.createObjectBuilder();

						var reader = Json.createReader(new StringReader(json));
						var root = reader.readObject().asJsonObject();
						root.getJsonArray("dependencies").forEach(key -> {
							var id = ((JsonString) key).getString();
							var item = Json.createObjectBuilder();
							switch (id) {
							case "npm/npmjs/-/write/0.2.0":
								item.add("authority", "CQ7766");
								item.add("confidence", 100);
								item.add("id", id);
								item.add("license", "Apache-2.0");
								item.addNull("sourceUrl");
								item.add("status", "approved");

								approved.add(id, item);
								break;

							case "npm/npmjs/@yarnpkg/lockfile/1.1.0":
								item.add("authority", "CQ7722");
								item.add("confidence", 100);
								item.add("id", id);
								item.add("license", "GPL-2.0");
								item.addNull("sourceUrl");
								item.add("status", "rejected");

								rejected.add(id, item);
								break;
							}
						});

						var parent = Json.createObjectBuilder();
						parent.add("approved", approved);
						parent.add("rejected", rejected);

						handler.accept(parent.build().toString());

						return 200;
					}
				}
				return 404;
			}

			private void appendFileContents(StringBuilder builder, String name) {
				try (BufferedReader writeFileReader = new BufferedReader(new InputStreamReader(
						this.getClass().getResourceAsStream(name),
						StandardCharsets.UTF_8))) {
					builder.append(writeFileReader.lines().collect(Collectors.joining("\n")));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public int get(String url, String contentType, Consumer<InputStream> handler) {
				if (url.equals(settings.getApprovedLicensesUrl())) {
					handler.accept(this.getClass().getResourceAsStream("/licenses.json"));
					return 200;
				}

				switch (url) {
				case "https://registry.npmjs.org/chalk":
					handler.accept(this.getClass().getResourceAsStream("/chalk.json"));
					return 200;
				case "https://pypi.org/pypi/asn1crypto/json":
					handler.accept(this.getClass().getResourceAsStream("/test_data_asn1crypto.json"));
					return 200;
				case "https://raw.githubusercontent.com/clearlydefined/service/HEAD/schemas/curation-1.0.json":
					handler.accept(this.getClass().getResourceAsStream("/test_data_curation-1.0.json"));
					return 200;
				}

				return 404;
			}

			@Override
			public boolean remoteFileExists(String url) {
				switch (url) {
				case "https://search.maven.org/artifact/group.path/artifact/1.0/jar":
				case "https://search.maven.org/remotecontent?filepath=group/path/artifact/1.0/artifact-1.0-sources.jar":
				case "https://github.com/sindresorhus/chalk/archive/v0.1.0.zip":
				case "https://registry.npmjs.org/chalk/-/chalk-0.1.0.tgz":
					return true;
				}
				return IHttpClientService.super.remoteFileExists(url);
			}
		};
	}
}
