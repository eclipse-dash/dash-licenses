/*************************************************************************
 * Copyright (c) 2021 Oleksandr Andriienko
 * Copyright (c) 2022 The Eclipse Foundation
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.golang;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JsoupProvider {

	public Document getDocument(String url) throws IOException {
		String githubToken = System.getenv("GITHUB_TOKEN");
		Connection connection = Jsoup
				.connect(url)
				.header("Authorization", "bearer " + githubToken)
				.userAgent("Mozilla")
				.followRedirects(true)
				.timeout(20000);
		Connection.Response resp = connection.execute();
		if (resp.statusCode() == 200) {
			return connection.get();
		}
		return null;
	}
}
