/*************************************************************************
 * Copyright (c) 2022 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

import java.net.http.HttpClient;

/**
 * Injectable HTTP proxy settings.
 */
public interface IProxySettings {

	/** The default port number ({@value}) for HTTP proxy. */
	int DEFAULT_PROXY_PORT = 8080;

	/**
	 * Configure an HTTP client for a proxy server.
	 * 
	 * @param httpClientBuilder the HTTP client builder to configure
	 */
	void configure(HttpClient.Builder httpClientBuilder);

}