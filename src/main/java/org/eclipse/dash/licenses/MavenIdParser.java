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
	
	private static final String MAVEN_PATTERN = 
			"(?<groupid>[^: ]+)"
			+ ":(?<artifactid>[^: ]+)"
			+ "(:(?<ext>[^: ]*)(:(?<classifier>[^: ]+))?)?"
			+ ":(?<version>\\d+(\\.\\d+){0,2})(?:\\.(?<qualifer>\\d{8,}))?[^: ]*"
			+ "(:(?<phase>[^:]*))?";
	private static Pattern mavenPattern = Pattern.compile(MAVEN_PATTERN);

	@Override
	public Optional<IContentId> parseId(String value) {
		// TODO Deal with (non-standard?) formats
		// e.g. p2.eclipse-plugin:org.jaxen:jar:lib/jaxen-1.1.6.jar:1.1.6.201804090728:system
		// I'm not quite sure how this should map. It's the "jaxen-1.1.6" part that I think is
		// important, I don't think that we can automagically map this to actual valid Maven
		// coordinates, but we may be able to map it to something that we can match with
		// EF data. We might, for example, try to do some sort of bundle mapping with (for this
		// particular example, CQ 11159.
		Matcher matcher = mavenPattern.matcher(value.trim());
		if (!matcher.matches()) return Optional.empty();

		String groupid = matcher.group("groupid");
		String artifactid = matcher.group("artifactid");
		String version = matcher.group("version");

		String type = groupid.startsWith("p2.eclipse-") ? "p2" : "maven";
		String source = groupid.startsWith("p2.eclipse-") ? "orbit" : "mavencentral";
		
		return Optional.of(new ContentId(type, source, groupid, artifactid, version));
	}
}
