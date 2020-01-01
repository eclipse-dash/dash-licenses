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

public class MavenIdParser implements ContentIdParser {
	
	private static Pattern mavenPattern = Pattern
		.compile("\\s*"
				+ "(?<groupid>[^: ]+)"
				+ ":(?<artifactid>[^: ]+)"
				+ "(:(?<ext>[^: ]*)(:(?<classifier>[^: ]+))?)?"
				+ ":(?<version>\\d+(.\\d+)*)[^: ]*"
				+ "(:[^:]*)?\\s*");

	@Override
	public Optional<IContentId> parseId(String value) {
		Matcher matcher = mavenPattern.matcher(value);
		if (!matcher.matches()) return Optional.empty();

		String groupid = matcher.group("groupid");
		String type = groupid.startsWith("p2.eclipse-") ? "p2" : "maven";
		String source = groupid.startsWith("p2.eclipse-") ? "orbit" : "mavencentral";
		
		return Optional.of(new ContentId(type, source, groupid, matcher.group("artifactid"), matcher.group("version")));
	}
}
