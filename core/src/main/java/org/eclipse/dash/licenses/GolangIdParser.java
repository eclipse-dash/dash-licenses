/*************************************************************************
 * Copyright (c) 2021,2022 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

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

	// @formatter:off
	private static Pattern recordPattern = Pattern.compile(
			"^(?:(?<namespace>[^\\s]+)\\/)?(?<name>[^\\/\\s]+)\\s(?<version>v[^\\s\\/+]+)(?<plus>\\+[^\\/]+)?(?<go>\\/go\\.mod)?(?:\\sh\\d:(?<sha>[^=]+)=)?(?:\\s+\\/\\/.*)?$"
	);
	// @formatter:on

	@Override
	public IContentId parseId(String value) {
		Matcher matcher = recordPattern.matcher(value.trim());
		if (!matcher.matches())
			return null;

		String namespace = matcher.group("namespace");
		if (namespace == null) {
			namespace = "-";
		} else {
			namespace = namespace.replace("/", "%2F");
		}

		String name = matcher.group("name");
		String version = matcher.group("version");

		return ContentId.getContentId("go", "golang", namespace, name, version);
	}
}
