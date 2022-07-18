/*************************************************************************
 * Copyright (c) 2021,2022 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.extended;

import javax.inject.Inject;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.http.IHttpClientService;

public class MavenCentralExtendedContentDataProvider implements IExtendedContentDataProvider {
	@Inject
	ISettings settings;
	@Inject
	IHttpClientService httpClientService;

	static String[] sourcePathPatterns = new String[] {
			"https://search.maven.org/remotecontent?filepath={groupPath}/{artifactid}/{version}/{artifactid}-{version}-sources.jar",
			"https://search.maven.org/remotecontent?filepath={groupPath}/{artifactid}/{version}/{artifactid}-{version}-src.zip" };

	static String[] pomPathPatterns = new String[] {
			"https://search.maven.org/remotecontent?filepath={groupPath}/{artifactid}/{version}/{artifactid}-{version}.pom" };

	@Override
	public ExtendedContentData getExtendedContentData(IContentId id) {
		if (!"maven".equals(id.getType()))
			return null;
		if (!"mavencentral".equals(id.getSource()))
			return null;

		return new MavenCentralPackageBuilder(id).build();
	}

	@Override
	public String getSourceUrl(IContentId id) {
		return new MavenCentralPackageBuilder(id).getSourceUrl();
	}

	public class MavenCentralPackageBuilder {
		private IContentId id;

		public MavenCentralPackageBuilder(IContentId id) {
			this.id = id;
		}

		public ExtendedContentData build() {
			var sourceUrl = getSourceUrl();
			var pomUrl = getPomUrl();

			// Experimental: If there's no source, but there is a pom.xml, and
			// the referenced thing is a "bill of materials" (BOM), use the pom.xml
			// as the source.
			// FIXME determine if there is a better way to know that it's a BOM.
			if (sourceUrl == null && pomUrl != null && id.getName().endsWith("-bom")) {
				sourceUrl = pomUrl;
			}

			if (sourceUrl == null && pomUrl == null)
				return null;

			var thing = new ExtendedContentData("Maven Central", getUrl());

			thing.addItem("ID", id.toString());
			thing.addLink("Source", sourceUrl);
			thing.addLink("POM", pomUrl);

			return thing;
		}

		public String getUrl() {
			return String
					.format("https://search.maven.org/artifact/%s/%s/%s/jar", id.getNamespace(), id.getName(),
							id.getVersion());
		}

		public String getSourceUrl() {
			for (String pattern : sourcePathPatterns) {
				var url = pattern;
				url = url.replace("{groupPath}", id.getNamespace().replace('.', '/'));
				url = url.replace("{artifactid}", id.getName());
				url = url.replace("{version}", id.getVersion());

				if (httpClientService.remoteFileExists(url)) {
					return url;
				}
			}
			return null;
		}

		public String getPomUrl() {
			for (String pattern : pomPathPatterns) {
				var url = pattern
						.replace("{groupPath}", id.getNamespace().replace('.', '/'))
						.replace("{artifactid}", id.getName())
						.replace("{version}", id.getVersion());

				if (httpClientService.remoteFileExists(url)) {
					return url;
				}
			}
			return null;
		}
	}
}
