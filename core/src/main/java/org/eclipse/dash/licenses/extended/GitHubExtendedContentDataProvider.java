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

import javax.inject.Inject;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.http.IHttpClientService;

public class GitHubExtendedContentDataProvider implements IExtendedContentDataProvider {

	@Inject
	IHttpClientService httpClientService;

	static String[] sourcePathPatterns = new String[] {
			"https://github.com/{namespace}/{name}/archive/refs/tags/release/{revision}.zip",
			"https://github.com/{namespace}/{name}/archive/refs/tags/{revision}.zip",
			"https://github.com/{namespace}/{name}/archive/refs/tags/v{revision}.zip" };

	@Override
	public ExtendedContentData getExtendedContentData(IContentId id) {
		if (!appliesTo(id))
			return null;

		return new GitHubPackageBuilder(id).build();
	}

	@Override
	public String getSourceUrl(IContentId id) {
		return new GitHubPackageBuilder(id).getSourceUrl();
	}

	private boolean appliesTo(IContentId id) {
		if (!"git".equals(id.getType()))
			return false;
		if (!"github".equals(id.getSource()))
			return false;
		return true;
	}

	class GitHubPackageBuilder {
		private IContentId id;

		public GitHubPackageBuilder(IContentId id) {
			this.id = id;
		}

		public ExtendedContentData build() {
			var thing = new ExtendedContentData("GitHub", getUrl());
			thing.addLink("Source", getSourceUrl());

			return thing;
		}

		public String getUrl() {
			return String.format("https://github.com/%s/%s", id.getNamespace(), id.getName(), id.getVersion());
		}

		public String getSourceUrl() {
			for (String pattern : sourcePathPatterns) {
				var url = pattern;
				url = url.replace("{namespace}", id.getNamespace().replace('.', '/'));
				url = url.replace("{name}", id.getName());
				url = url.replace("{revision}", id.getVersion());

				if (httpClientService.remoteFileExists(url)) {
					return url;
				}
			}
			return null;
		}
	}
}
