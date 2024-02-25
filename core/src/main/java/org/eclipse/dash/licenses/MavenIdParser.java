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
import static org.eclipse.dash.licenses.util.MavenCoordinatesParser.parse;

public class MavenIdParser implements ContentIdParser {
	/*
	 * We set the type and source to "p2" and "orbit" when we encounter something
	 * with a p2 prefix. We had previously grabbed this value directly from Tycho
	 * (<code>org.eclipse.tycho:org.eclipse.tycho.embedder.shared</code>) but doing
	 * so periodically introduced incompatibilities after updates. Also... we really
	 * only need this value and really don't expect it to change, so pulling in even
	 * just an entire class seems excessive.
	 */
	private static final String P2_GROUPID_PREFIX = "p2.";

	private Pattern antBundleClassifierPattern = Pattern.compile("lib/(?<artifactid>.*)\\.jar");
	private Pattern semanticVersionPattern = Pattern.compile("(?<version>\\d+(?:\\.\\d+){1,2}).*");

	@Override
	public IContentId parseId(String value) {
		/*
		 * We originally implemented this using Aether, but Aether breaks when a phase
		 * is included in the value. So, we brute force it.
		 */
		
		var mavenCoordinates = parse(value);
		if (mavenCoordinates == null) return null;

		String type = mavenCoordinates.groupId.startsWith(P2_GROUPID_PREFIX) ? "p2" : "maven";
		String source = mavenCoordinates.groupId.startsWith(P2_GROUPID_PREFIX) ? "orbit" : "mavencentral";

		/*
		 * So this is a complete hack. If we're looking at the Apache Ant bundle, then
		 * use the classifier to sort out the actual Maven GAV. 
		 */
		// FIXME Find a more general solution
		if ("p2".equals(type) && "org.apache.ant".equals(mavenCoordinates.artifactId) && !mavenCoordinates.classifier.isEmpty()) {
			Matcher classifierMatcher = antBundleClassifierPattern.matcher(mavenCoordinates.classifier);
			if (classifierMatcher.matches()) {
				type = "maven";
				source = "mavencentral";
				var groupid = mavenCoordinates.artifactId;
				var artifactid = classifierMatcher.group("artifactid");
				var version = mavenCoordinates.version;
				
				Matcher semanticVersionMatcher = semanticVersionPattern.matcher(version);
				if (semanticVersionMatcher.matches()) {
					version = semanticVersionMatcher.group("version");
				}
				return ContentId.getContentId(type, source, groupid, artifactid, version);
			}
		}

		return ContentId.getContentId(type, source, mavenCoordinates.groupId, mavenCoordinates.artifactId, mavenCoordinates.version);
	}
}
