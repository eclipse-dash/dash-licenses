/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse an ID expressed in the go.sum file format.
 *
 * <p>
 * See the <a href="https://golang.org/ref/mod#go-sum-files">go.sum file
 * format</a> for more information. See discussion regarding the
 * <a href="https://github.com/clearlydefined/service/pull/871">form of the
 * ClearlyDefined IDs</a>.
 */
public class GolangIdParser implements ContentIdParser {

	private static Pattern recordPattern = Pattern.compile(
	// @formatter:off
			"^(?<source>[^\\/\\s]+)(?:\\/(?<path>[^\\/\\s]+)(?:\\/(?<module>[^\\/\\s]+))?)?(?:\\/[^\\/\\s]+)?\\s(?<version>v[^\\s\\/+]+).*$"
	);

	Pattern refPattern = Pattern
			.compile("^v(?<version>\\d+\\.\\d+\\.\\d+)-(?:\\d\\.)?(?<qualifier>\\d{14})-(?<ref>[\\da-f]{12})$");
	// @formatter:on

	@Override
	public IContentId parseId(String value) {
		Matcher matcher = recordPattern.matcher(value.trim());
		if (!matcher.matches())
			return null;

		String source = matcher.group("source");
		String path = matcher.group("path");
		String module = matcher.group("module");
		String version = matcher.group("version");

		String namespace, name;

		if (path == null) {
			namespace = "-";
			name = source;
		} else if (module == null) {
			namespace = source;
			name = path;
		} else {
			namespace = source + "/" + path;
			name = module;
		}

		/*
		 * In cases where the version takes the form
		 * "v0.0.0-20190423205320-6a90982ecee2", we're likely looking at an abridged Git
		 * commit ref ("6a90982ecee2"), so let's boil it down to that.
		 */
		Matcher refMatcher = refPattern.matcher(version);
		if (refMatcher.matches()) {
			version = refMatcher.group("ref");
		}

		if ("github.com".equals(source)) {
			return ContentId.getContentId("git", "github", path.toLowerCase(), module, version);
		}

		if ("golang.org".equals(source) && "x".equals(path)) {
			return ContentId.getContentId("git", "github", "golang", module, version);
		}

		return ContentId
				.getContentId("go", "golang", URLEncoder.encode(namespace, Charset.defaultCharset()), name, version);
	}
}
