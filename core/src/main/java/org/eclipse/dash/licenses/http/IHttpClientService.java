/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.http;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Implementers of this class provide an abstract means of making HTTP requests.
 */
public interface IHttpClientService {

	/**
	 * Make an HTTP POST Request. The handler is only invoked when the response is
	 * 200.
	 * 
	 * @param url         The target URL.
	 * @param contentType The MIME time expected in the response
	 * @param payload     The HTTP Request content (i.e., what gets sent to the
	 *                    server)
	 * @param handler     A consumer for the response content.
	 * @return the HTTP response code
	 */
	default int post(String url, String contentType, String payload, Consumer<String> handler) {
		return 500;
	};

	default boolean remoteFileExists(String url) {
		return false;
	}

	default int get(String url, String contentType, Consumer<InputStream> handler) {
		return 500;
	}

	default int get(String url, String contentType, Map<String, String> headers, Consumer<InputStream> handler) {
		return 500;
	}

	default String exists(String url) {
		if (url == null) return null;
		if (remoteFileExists(url)) {
			return url;
		}
		return null;
	}
}
