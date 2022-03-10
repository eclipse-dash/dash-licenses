/*************************************************************************
 * Copyright (c) 2022 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.maven;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.text.MessageFormat;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.dash.licenses.IProxySettings;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

public class MavenProxySettings implements IProxySettings {

	static {
		// Re-enable the Basic authentication scheme
		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
	}

	private final InetSocketAddress proxyAddress;
	private final String username;
	private final String password;

	private final SecDispatcher securityDispatcher;
	private final Log log;

	public MavenProxySettings(String proxyHost, int proxyPort, String username, String password,
			SecDispatcher securityDispatcher, Log log) {

		super();

		this.proxyAddress = new InetSocketAddress(proxyHost, proxyPort);
		this.username = username;
		this.password = password;

		this.securityDispatcher = securityDispatcher;
		this.log = log;
	}

	@Override
	public void configure(HttpClient.Builder httpClientBuilder) {
		ProxySelector proxySelector = ProxySelector.of(proxyAddress);

		httpClientBuilder.proxy(proxySelector);
		if (username != null && password != null) {
			httpClientBuilder.authenticator(new ProxyAuthenticator());
		}
	}

	private class ProxyAuthenticator extends Authenticator {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			if (!getRequestingHost().equals(proxyAddress.getAddress().getHostName())) {
				return null;
			}

			log.debug("Request to authenticate proxy " + getRequestingHost());

			String password = MavenProxySettings.this.password;
			// Try to decrypt it
			try {
				password = securityDispatcher.decrypt(password);
			} catch (final SecDispatcherException e) {
				log.warn(MessageFormat.format(
						"Failed to decrypt password for proxy server: {0}\nPassword will be attempted verbatim.",
						e.getMessage()));
			}

			return new PasswordAuthentication(username, password.toCharArray());
		}
	}
}