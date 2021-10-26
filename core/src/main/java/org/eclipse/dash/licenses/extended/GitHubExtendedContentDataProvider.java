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

	@Override
	public ExtendedContentData getExtendedContentData(IContentId id) {

		if (!"git".equals(id.getType()))
			return null;
		if (!"github".equals(id.getSource()))
			return null;

		return new GitHubPackageBuilder(id).build();
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
			var url = String.format("https://github.com/%s/%s/archive/refs/tags/%s.zip", id.getNamespace(),
					id.getName(), id.getVersion());

			if (httpClientService.remoteFileExists(url)) {
				return url;
			}

			return null;
		}
	}
}
