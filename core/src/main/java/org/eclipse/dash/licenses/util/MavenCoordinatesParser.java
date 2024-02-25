/*************************************************************************
 * Copyright (c) 2024 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.util;

/**
 * Maven GAVs, expressed in "colon format", come with between three and six
 * segments.
 * 
 * <pre>groupId:artifactId[:type[:classifier]?]?:version[:scope]?</pre>
 * 
 * The <code>groupId</code>, <code>artifactId</code>, and <code>version</code>
 * are always present. A <code>type</code> may be present; a
 * <code>classifier</code> may also be present, but only when the
 * <code>type</code> is expressed. The <code>scope</code> may be expressed as
 * the last segment. When the <code>scope</code> is not present, the
 * <code>version</code> is in the last segment; when the <code>scope</code> is
 * present, the <code>version</code> is in the second-to-last segment.
 * 
 * <p>
 * The <code>scope</code> must be one of:
 * <ul>
 * <li><code>compile</code></li>
 * <li><code>provided</code></li>
 * <li><code>runtime</code></li>
 * <li><code>test</code></li>
 * <li><code>system</code></li>
 * </ul>
 * 
 * Some examples (from the Maven Dependencies plugin):
 * 
 * <pre> org.glassfish:jakarta.json:jar:2.0.1:compile
 * org.eclipse.jgit:org.eclipse.jgit:jar:6.8.0.202311291450-r:compile
 * org.glassfish.jersey.ext:jersey-entity-filtering:jar:2.39.1:compile
 * com.fasterxml.jackson.module:jackson-module-jaxb-annotations:jar:2.14.1:compile
 * com.googlecode.javaewah:JavaEWAH:jar:1.2.3:compile
 * org.gitlab4j:gitlab4j-api:jar:5.4.0:compile
 * com.google.inject.extensions:guice-multibindings:jar:4.2.3:compile
 * com.fasterxml.jackson.core:jackson-databind:jar:2.14.1:compile
 * org.junit.platform:junit-platform-launcher:jar:1.10.1:test
 * commons-codec:commons-codec:jar:1.16.0:compile
 * org.junit.platform:junit-platform-engine:jar:1.10.1:test</pre>
 */
public class MavenCoordinatesParser {
	
	public static MavenCoordinates parse(String value) {
		var segments = value.trim().split(":");

		if (segments.length < 3) return null;
		
		String groupId = segments[0];
		String artifactId = segments[1];
		String type = "jar";
		String classifier = "";
		String version = "";
		String scope = "compile";
		
		var length = segments.length;
		
		if (isScope(segments[length-1])) {
			scope = segments[length-1];
			length--;
		}

		if (length < 3 || length > 5) return null;
		
		version = segments[length-1];
		if (length > 3) type = segments[2];
		if (length > 4) classifier = segments[3];
		
		return new MavenCoordinates(groupId, artifactId, type, classifier, version, scope);
	}
	
	private static boolean isScope(String value) {
		switch (value) {
		case "compile" :
		case "provided" :
		case "runtime" :
		case "test" :
		case "system" :
			return true;
		}
		return false;
	}
}
