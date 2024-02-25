/*************************************************************************
 * Copyright (c) 2024 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.dash.licenses.util.MavenCoordinatesParser;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MavenCoordinatesParserTests {

	@Nested
	class AbstractCoordinatesTests {
		@Test
		void test1() {
			var gav = MavenCoordinatesParser.parse("group:artifact:type:classifier:version:system");
			assertEquals("group", gav.groupId);
			assertEquals("artifact", gav.artifactId);
			assertEquals("type", gav.type);
			assertEquals("classifier", gav.classifier);
			assertEquals("version", gav.version);
			assertEquals("system", gav.scope);
		}
	
		@Test
		void test2() {
			var gav = MavenCoordinatesParser.parse("group:artifact:type:version:system");
			assertEquals("group", gav.groupId);
			assertEquals("artifact", gav.artifactId);
			assertEquals("type", gav.type);
			assertEquals("", gav.classifier);
			assertEquals("version", gav.version);
			assertEquals("system", gav.scope);
		}
	
		@Test
		void test3() {
			var gav = MavenCoordinatesParser.parse("group:artifact:version:system");
			assertEquals("group", gav.groupId);
			assertEquals("artifact", gav.artifactId);
			assertEquals("jar", gav.type);
			assertEquals("", gav.classifier);
			assertEquals("version", gav.version);
			assertEquals("system", gav.scope);
		}
	
		@Test
		void test4() {
			var gav = MavenCoordinatesParser.parse("group:artifact:version");
			assertEquals("group", gav.groupId);
			assertEquals("artifact", gav.artifactId);
			assertEquals("jar", gav.type);
			assertEquals("", gav.classifier);
			assertEquals("version", gav.version);
			assertEquals("compile", gav.scope);
		}
	
		@Test
		void test5() {
			var gav = MavenCoordinatesParser.parse("group:artifact:type:version");
			assertEquals("group", gav.groupId);
			assertEquals("artifact", gav.artifactId);
			assertEquals("type", gav.type);
			assertEquals("", gav.classifier);
			assertEquals("version", gav.version);
			assertEquals("compile", gav.scope);
		}
	}
	
	@Nested
	class RealCoordinatesTests {
		@Test
		void test1() {
			var gav = MavenCoordinatesParser.parse("p2.eclipse-plugin:org.apache.ant:jar:lib/ant-commons-net.jar:1.10.8.v20200515-1239:system");
			assertEquals("p2.eclipse-plugin", gav.groupId);
			assertEquals("org.apache.ant", gav.artifactId);
			assertEquals("jar", gav.type);
			assertEquals("lib/ant-commons-net.jar", gav.classifier);
			assertEquals("1.10.8.v20200515-1239", gav.version);
			assertEquals("system", gav.scope);
		}

		@Test
		void test2() {
			var gav = MavenCoordinatesParser.parse("com.google.javascript:closure-compiler-externs:jar:v20160315:compile");
			assertEquals("com.google.javascript", gav.groupId);
			assertEquals("closure-compiler-externs", gav.artifactId);
			assertEquals("jar", gav.type);
			assertEquals("", gav.classifier);
			assertEquals("v20160315", gav.version);
			assertEquals("compile", gav.scope);
		}

		@Test
		void test3() {
			var gav = MavenCoordinatesParser.parse("com.google.javascript:closure-compiler-externs:v20160315");
			assertEquals("com.google.javascript", gav.groupId);
			assertEquals("closure-compiler-externs", gav.artifactId);
			assertEquals("jar", gav.type);
			assertEquals("", gav.classifier);
			assertEquals("v20160315", gav.version);
			assertEquals("compile", gav.scope);
		}

		@Test
		void test4() {
			var gav = MavenCoordinatesParser.parse("com.teketik:mock-in-bean:boot2-v1.5.2");
			assertEquals("com.teketik", gav.groupId);
			assertEquals("mock-in-bean", gav.artifactId);
			assertEquals("jar", gav.type);
			assertEquals("", gav.classifier);
			assertEquals("boot2-v1.5.2", gav.version);
			assertEquals("compile", gav.scope);
		}

		@Test
		void test5() {
			var gav = MavenCoordinatesParser.parse("org.junit.platform:junit-platform-launcher:jar:1.10.1:test");
			assertEquals("org.junit.platform", gav.groupId);
			assertEquals("junit-platform-launcher", gav.artifactId);
			assertEquals("jar", gav.type);
			assertEquals("", gav.classifier);
			assertEquals("1.10.1", gav.version);
			assertEquals("test", gav.scope);
		}
	}
	
	@Nested
	class InvalidCoordinatesTests {
		
		@Test
		void test1() {
			var gav = MavenCoordinatesParser.parse("group:artifact:provided");
			assertNull(gav);
		}

		
		@Test
		void test2() {
			var gav = MavenCoordinatesParser.parse("group:artifact");
			assertNull(gav);
		}
		
		@Test
		void test3() {
			var gav = MavenCoordinatesParser.parse("group:artifact:type:version:system:extra");
			assertNull(gav);
		}
	}
}
