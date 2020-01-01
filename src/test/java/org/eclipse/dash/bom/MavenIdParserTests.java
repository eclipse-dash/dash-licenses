package org.eclipse.dash.bom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.MavenIdParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MavenIdParserTests {
	private MavenIdParser parser;

	@BeforeEach
	public void setup() {
		parser = new MavenIdParser();
	}

	@Test
	public void testCreation1() {
		IContentId value = parser.parseId("groupid:artifactid:1.2.3").get();
		assertEquals("groupid", value.getNamespace());
		assertEquals("artifactid", value.getName());
		assertEquals("1.2.3", value.getVersion());
	}

	@Test
	public void testCreation2() {
		IContentId value = parser.parseId("groupid:artifactid:jar:1.2.3").get();
		assertEquals("groupid", value.getNamespace());
		assertEquals("artifactid", value.getName());
		assertEquals("1.2.3", value.getVersion());
	}

	@Test
	public void testCreation3() {
		IContentId value = parser.parseId("groupid:artifactid:jar:test:1.2.3").get();
		assertEquals("groupid", value.getNamespace());
		assertEquals("artifactid", value.getName());
		assertEquals("1.2.3", value.getVersion());
	}
	
	@Test
	public void testCreation4() {
		IContentId value = parser.parseId("groupid:artifactid:jar:1.2.3:compile").get();
		assertEquals("groupid", value.getNamespace());
		assertEquals("artifactid", value.getName());
		assertEquals("1.2.3", value.getVersion());
	}

	@Test
	public void testCreation5() {
		IContentId value = parser.parseId("  groupid:artifactid:jar:1.2.3:compile  ").get();
		assertEquals("groupid", value.getNamespace());
		assertEquals("artifactid", value.getName());
		assertEquals("1.2.3", value.getVersion());
	}

	@Test
	public void testCreation6() {
		IContentId value = parser.parseId("p2.eclipse-plugin:org.eclipse.core.jobs:jar:3.8.0.v20160509-0411").get();
		assertEquals("p2", value.getType());
		assertEquals("orbit", value.getSource());
		assertEquals("p2.eclipse-plugin", value.getNamespace());
		assertEquals("org.eclipse.core.jobs", value.getName());
		assertEquals("3.8.0", value.getVersion());
	}
}
