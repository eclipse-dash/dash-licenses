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

import org.eclipse.dash.licenses.PackageUrl;
import org.eclipse.dash.licenses.PackageUrlIdParser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class PackageUrlIdParserTests {

	@Test
	void testBasic1() {
		assertEquals("bitbucket/-/birkenfeld/pygments-main/244fd47e07d1014f0aed9c",
				new PackageUrlIdParser()
						.parseId("pkg:bitbucket/birkenfeld/pygments-main@244fd47e07d1014f0aed9c")
						.getClearlyDefinedId());
		assertEquals("deb/-/debian/curl/7.50.3-1",
				new PackageUrlIdParser().parseId("pkg:deb/debian/curl@7.50.3-1?arch=i386&distro=jessie").getClearlyDefinedId());
		assertEquals("npm/npmjs/-/foobar/12.3.1", new PackageUrlIdParser().parseId("pkg:npm/foobar@12.3.1").getClearlyDefinedId());
		assertEquals("maven/mavencentral/org.apache.xmlgraphics/batik-anim/1.9.1",
				new PackageUrlIdParser()
						.parseId("pkg:maven/org.apache.xmlgraphics/batik-anim@1.9.1?packaging=sources")
						.getClearlyDefinedId());
		assertEquals("rpm/-/opensuse/curl/7.56.1-1.1.",
				new PackageUrlIdParser()
						.parseId("pkg:rpm/opensuse/curl@7.56.1-1.1.?arch=i386&distro=opensuse-tumbleweed")
						.getClearlyDefinedId());
	}

	@Test @Disabled
	void testGeneric() {
		new PackageUrlIdParser().parseId("pkg:generic/fox@1.6.59?download_url=http://fox-toolkit.org/ftp/fox-1.6.59.zip&checksum=sha256:73d16c2bbd32f432bd6f07212d4eb83cfdb7005e0386640a5996752d1a6e3281");
	}
	
	@Test @Disabled
	void testWithDownload() {
		var id = new PackageUrlIdParser().parseId("pkg:github/apache/xerces-c@3.3.0?download_url=https://github.com/apache/xerces-c/archive/refs/tags/v3.3.0.zip");
		assertEquals("https://github.com/apache/xerces-c/archive/refs/tags/v3.3.0.zip", ((PackageUrl)id).getDownloadUrl());
	}
	
	@Test
	void testGitHub() {
		var id = new PackageUrlIdParser().parseId("pkg:github/osgeo/proj@9.5.0");
		assertEquals("git", id.getType());
		assertEquals("github", id.getSource());
		assertEquals("osgeo", id.getNamespace());
		assertEquals("proj", id.getName());
		assertEquals("9.5.0", id.getVersion());
	}
	
	@Test
	void testInvalid() {
		assertNull(new PackageUrlIdParser().parseId("@babel/highlight@7.5.0"));
		assertNull(new PackageUrlIdParser().parseId("highlight@7.5.0"));
	}

}
