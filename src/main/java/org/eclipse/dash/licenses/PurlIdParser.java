/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PurlIdParser implements ContentIdParser {

	private static Pattern purlPattern = Pattern.compile(
	// @formatter:off
			"\\s*" 
			+ "(?:(?<namespace>@\\S+)\\/)?" 
			+ "(?<name>\\S+)" 
			+ "@(?<version>[^@]+)" 
			+ "\\s*"
	// @formatter:on
	);

	@Override
	public Optional<IContentId> parseId(String value) {
		Matcher matcher = purlPattern.matcher(value);
		if (!matcher.matches())
			return Optional.empty();

		String namespace = matcher.group("namespace");
		if (namespace == null)
			namespace = "-";
		String name = matcher.group("name");
		String version = matcher.group("version");

		return Optional.of(new ContentId("npm", "npmjs", namespace, name, version));
	}
}
