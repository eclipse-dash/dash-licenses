/*************************************************************************
 * Copyright (c) 2019,2023 The Eclipse Foundation and others.
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

public class MavenIdParser implements ContentIdParser {

	// @formatter:off
	private static final String MAVEN_PATTERN =
			"(?<groupid>[^: ]+)"
			+ ":(?<artifactid>[^: ]+)"
			+ "(:(?<ext>[^: ]*)(:(?<classifier>[^: ]+))?)?"
			+ ":(?<version>v?\\d[^: ]*)"
			+ "(:(?<phase>[^:]*))?";
	// @formatter:on

	/*
	 * We set the type and source to "p2" and "orbit" when we encounter something
	 * with a p2 prefix. We had previously grabbed this value directly from Tycho
	 * (<code>org.eclipse.tycho:org.eclipse.tycho.embedder.shared</code>) but doing
	 * so periodically introduced incompatibilities after updates. Also... we really
	 * only need this value and really don't expect it to change, so pulling in even
	 * just an entire class seems excessive.
	 */
	private static final String P2_GROUPID_PREFIX = "p2.";

	private static Pattern mavenPattern = Pattern.compile(MAVEN_PATTERN);
	private Pattern antBundleClassifierPattern = Pattern.compile("lib/(?<artifactid>.*)\\.jar");
	private Pattern semanticVersionPattern = Pattern.compile("(?<version>\\d+(?:\\.\\d+){1,2}).*");

	@Override
	public IContentId parseId(String value) {
		// TODO Deal with (non-standard?) formats
		/*
		 * e.g.
		 * p2.eclipse-plugin:org.jaxen:jar:lib/jaxen-1.1.6.jar:1.1.6.201804090728:system
		 * I'm not quite sure how this should map. It's the "jaxen-1.1.6" part that I
		 * think is important, I don't think that we can automagically map this to
		 * actual valid Maven coordinates, but we may be able to map it to something
		 * that we can match with EF data. We might, for example, try to do some sort of
		 * bundle mapping with (for this particular example, CQ 11159).
		 *
		 * We originally implemented this using Aether, but Aether breaks when a phase
		 * is included in the value. So, we brute force it with a regular expression.
		 */
		Matcher matcher = mavenPattern.matcher(value.trim());
		if (!matcher.matches())
			return null;

		String groupid = matcher.group("groupid");
		String artifactid = matcher.group("artifactid");
		String version = matcher.group("version");

		String type = groupid.startsWith(P2_GROUPID_PREFIX) ? "p2" : "maven";
		String source = groupid.startsWith(P2_GROUPID_PREFIX) ? "orbit" : "mavencentral";

		/*
		 * So this is a complete hack. If we're looking at the Apache Ant bundle, then
		 * use the classifier to sort out the actual Maven GAV. This works for Ant, but
		 * may not work in the general case.
		 */
		// FIXME Find a more general solution
		if ("p2".equals(type) && "org.apache.ant".equals(artifactid)) {
			Matcher classifierMatcher = antBundleClassifierPattern.matcher(matcher.group("classifier"));
			if (classifierMatcher.matches()) {
				type = "maven";
				source = "mavencentral";
				groupid = artifactid;
				artifactid = classifierMatcher.group("artifactid");

				Matcher semanticVersionMatcher = semanticVersionPattern.matcher(version);
				if (semanticVersionMatcher.matches()) {
					version = semanticVersionMatcher.group("version");
				}
			}
		}

		return ContentId.getContentId(type, source, groupid, artifactid, version);
	}
}
