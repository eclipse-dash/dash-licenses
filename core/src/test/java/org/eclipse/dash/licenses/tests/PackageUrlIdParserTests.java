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
	void testBasic1() {
		assertEquals("bitbucket/-/birkenfeld/pygments-main/244fd47e07d1014f0aed9c",
				new PackageUrlIdParser()
						.parseId("pkg:bitbucket/birkenfeld/pygments-main@244fd47e07d1014f0aed9c")
						.toString());
		assertEquals("deb/-/debian/curl/7.50.3-1",
				new PackageUrlIdParser().parseId("pkg:deb/debian/curl@7.50.3-1?arch=i386&distro=jessie").toString());
		assertEquals("npm/npmjs/-/foobar/12.3.1", new PackageUrlIdParser().parseId("pkg:npm/foobar@12.3.1").toString());
		assertEquals("maven/mavencentral/org.apache.xmlgraphics/batik-anim/1.9.1",
				new PackageUrlIdParser()
						.parseId("pkg:maven/org.apache.xmlgraphics/batik-anim@1.9.1?packaging=sources")
						.toString());
		assertEquals("rpm/-/opensuse/curl/7.56.1-1.1.",
				new PackageUrlIdParser()
						.parseId("pkg:rpm/opensuse/curl@7.56.1-1.1.?arch=i386&distro=opensuse-tumbleweed")
						.toString());
	}

	@Test
	void testBasic2() {
		assertNull(new PackageUrlIdParser().parseId("@babel/highlight@7.5.0"));
		assertNull(new PackageUrlIdParser().parseId("highlight@7.5.0"));
	}

	@Test
	void testBasic3() {
		assertEquals("git/github/itm/shawn%23src%2Fapps%2Ftcpip/45ff42d775dd72ab6819524988031719a75b206a",
				new PackageUrlIdParser().parseId("pkg:github/itm/shawn@45ff42d775dd72ab6819524988031719a75b206a#src/apps/tcpip").toString());
	}

	@Test
	void testBunch() {
		var values = new String[][] {
				{"generic/-/-/bzip2/1.0.8","pkg:generic/bzip2@1.0.8?download_url=https://sourceware.org/pub/bzip2/bzip2-1.0.8.tar.gz"},
				{"generic/-/-/eigen/3.4.0","pkg:generic/eigen@3.4.0?download_url=https://gitlab.com/libeigen/eigen/-/archive/3.4.0/eigen-3.4.0.zip"},
				{"generic/-/-/fox/1.6.59","pkg:generic/fox@1.6.59?download_url=http://fox-toolkit.org/ftp/fox-1.6.59.zip&checksum=sha256:73d16c2bbd32f432bd6f07212d4eb83cfdb7005e0386640a5996752d1a6e3281"},
				{"generic/-/-/freexl/2.0.0","pkg:generic/freexl@2.0.0?download_url=https://www.gaia-gis.it/gaia-sins/freexl-2.0.0.zip&checksum=sha256:ccac8445f1f939b31f61a0ac92425e3fcbddc62e0369f53422649417130c05b0"},
				{"generic/-/-/gettext%23gettext-runtime%2Fintl/v0.21","pkg:generic/gettext@v0.21?download_url=https://ftp.gnu.org/pub/gnu/gettext/gettext-0.21.tar.gz#gettext-runtime/intl"},
				
				{"git/github/apache/xerces-c/v3.3.0","pkg:github/apache/xerces-c@v3.3.0"},
				{"git/github/curl/curl/curl-8_10_1","pkg:github/curl/curl@curl-8_10_1"},
				{"git/github/dlr-ts/odrspiral/aecce087488b515332ec86d59b24076e06410c15","pkg:github/DLR-TS/odrSpiral@aecce087488b515332ec86d59b24076e06410c15"},
				{"git/github/itm/shawn%23src%2Fapps%2Ftcpip/45ff42d775dd72ab6819524988031719a75b206a","pkg:github/itm/shawn@45ff42d775dd72ab6819524988031719a75b206a#src/apps/tcpip"},

				{"maven/mavencentral/org.jboss.logging/jboss-logging/3.4.2.Final","pkg:maven/org.jboss.logging/jboss-logging@3.4.2.Final"},
				{"maven/mavencentral/org.openjfx/javafx-base/17.0.2","pkg:maven/org.openjfx/javafx-base@17.0.2"},
				{"maven/mavencentral/org.openjfx/javafx-graphics/17.0.2","pkg:maven/org.openjfx/javafx-graphics@17.0.2"}
		};
		for(int index=0;index<values.length;index++) {
			var pair = values[index];
			assertEquals(pair[0],new PackageUrlIdParser().parseId(pair[1]).toString());
		}
	}

}
