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
import java.net.URI;
import java.net.http.HttpClient;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.dash.licenses.IProxySettings;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

public class MavenProxySettings implements IProxySettings {

	/** Default protocol if not properly configured. The value is {@code "http"} as defaulted by Maven. */
	private static String DEFAULT_PROXY_PROTOCOL = "http";
	
	/** Pattern for lenient parsing of a protocol from a string that, for example, maybe includes the colon. */ 
	private static Pattern PROTOCOL_PATTERN = Pattern.compile("^\\w+");
	
	static {
		// Re-enable the Basic authentication scheme
		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
	}

	private final URI proxyURI;
	private final InetSocketAddress proxyAddress;
	private final String username;
	private final String password;

	private final SecDispatcher securityDispatcher;
	private final Log log;

	public MavenProxySettings(String proxyProtocol, String proxyHost, int proxyPort, String username, String password,
			SecDispatcher securityDispatcher, Log log) {

		super();

		this.proxyURI = URI.create(String.format("%s://%s:%s", sanitizeProtocol(proxyProtocol), proxyHost, proxyPort));
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
	
	/**
	 * Default the proxy {@code protocol} if it is not configured or if it is malformed.
	 * Otherwise, be tolerant if the protocol includes, for example, the colon as in
	 * {@code "http:"}.
	 * 
	 * @param protocol the proxy protocol as configured in the Maven settings
	 * @return a sanitized, possibly defaulted, protocol based on the input
	 */
	private static String sanitizeProtocol(String protocol) {
		if (protocol == null || protocol.isBlank()) {
			return DEFAULT_PROXY_PROTOCOL;
		}
		
		Matcher matcher = PROTOCOL_PATTERN.matcher(protocol);
		return matcher.find() ? matcher.group() : DEFAULT_PROXY_PROTOCOL;
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