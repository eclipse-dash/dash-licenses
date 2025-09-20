/*************************************************************************
 * Copyright (c) 2019 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.dash.licenses.PackageUrlIdParser;
import org.junit.jupiter.api.Test;

class PackageUrlIdParserTests {

	@Test
	void testToString() {
		assertEquals("pkg:bitbucket/birkenfeld/pygments-main@244fd47e07d1014f0aed9c",
				new PackageUrlIdParser()
						.parseId("pkg:bitbucket/birkenfeld/pygments-main@244fd47e07d1014f0aed9c")
						.toString());
		assertEquals("pkg:deb/debian/curl@7.50.3-1?arch=i386&distro=jessie",
				new PackageUrlIdParser().parseId("pkg:deb/debian/curl@7.50.3-1?arch=i386&distro=jessie").toString());
		assertEquals("pkg:npm/foobar@12.3.1", new PackageUrlIdParser().parseId("pkg:npm/foobar@12.3.1").toString());
		assertEquals("pkg:maven/org.apache.xmlgraphics/batik-anim@1.9.1?packaging=sources",
				new PackageUrlIdParser()
						.parseId("pkg:maven/org.apache.xmlgraphics/batik-anim@1.9.1?packaging=sources")
						.toString());
		assertEquals("pkg:rpm/opensuse/curl@7.56.1-1.1.?arch=i386&distro=opensuse-tumbleweed",
				new PackageUrlIdParser()
						.parseId("pkg:rpm/opensuse/curl@7.56.1-1.1.?arch=i386&distro=opensuse-tumbleweed")
						.toString());
	}

	@Test
	void testToClearlyDefined() {
		assertEquals("bitbucket/-/birkenfeld/pygments-main/244fd47e07d1014f0aed9c",
				new PackageUrlIdParser()
						.parseId("pkg:bitbucket/birkenfeld/pygments-main@244fd47e07d1014f0aed9c")
						.toClearlyDefined());
		assertEquals("deb/-/debian/curl/7.50.3-1",
				new PackageUrlIdParser().parseId("pkg:deb/debian/curl@7.50.3-1?arch=i386&distro=jessie").toClearlyDefined());
		assertEquals("npm/npmjs/-/foobar/12.3.1", new PackageUrlIdParser().parseId("pkg:npm/foobar@12.3.1").toClearlyDefined());
		assertEquals("maven/mavencentral/org.apache.xmlgraphics/batik-anim/1.9.1",
				new PackageUrlIdParser()
						.parseId("pkg:maven/org.apache.xmlgraphics/batik-anim@1.9.1?packaging=sources")
						.toClearlyDefined());
		assertEquals("rpm/-/opensuse/curl/7.56.1-1.1.",
				new PackageUrlIdParser()
						.parseId("pkg:rpm/opensuse/curl@7.56.1-1.1.?arch=i386&distro=opensuse-tumbleweed")
						.toClearlyDefined());
	}

	@Test
	void testInvalid() {
		assertNull(new PackageUrlIdParser().parseId("@babel/highlight@7.5.0"));
		assertNull(new PackageUrlIdParser().parseId("highlight@7.5.0"));
	}

}
