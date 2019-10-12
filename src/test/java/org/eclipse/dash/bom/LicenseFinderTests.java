package org.eclipse.dash.bom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LicenseFinderTests {

	private ContentRegistry registry;
	private ContentResolver resolver;

	@BeforeEach
	void setupRegistry() {
		String[][] data = {
			{"maven/mavencentral/org.bouncycastle/bcpkix-jdk15on/1.60","MIT", "approved"},
			{"maven/mavencentral/io.jaegertracing/jaeger-thrift/0.32.0", "Apache-2.0", "approved"},
			{"maven/mavencentral/org.jgroups.kubernetes/kubernetes/0.9.3", "Apache-2.0", "approved"},
			{"maven/mavencentral/org.jgroups.kubernetes/common/0.9.3", "Apache-2.0", "approved"},
			{"maven/mavencentral/ch.qos.logback/logback-access/1.2.1", "EPL-1.0", "approved"},
			{"maven/mavencentral/net.java.dev.jna/jna-platform/4.1.0", "Apache-2.0 AND LGPL-2.1", "approved"},
			{"maven/mavencentral/org.junit.jupiter/junit-jupiter/5.5.2", "EPL-2.0", "approved"}
		};
		
		registry = new InMemoryRegistry();
		for(String[] csv : data) {
			registry.cache(new BasicContentInfo(new ContentId(csv[0]), new License(csv[1]), csv[2]));
		}
	}
	
	@BeforeEach
	void setupResolver() {
		resolver = new ContentResolver() {
			public Results resolve(ContentId id) {
				switch (id.toString()) {
				case "maven/mavencentral/org.junit.jupiter/junit-jupiter/5.5.5":
					final ContentInfo info = new BasicContentInfo(id, new License("EPL-2.0"), "approved");
					return new Results() {
						public List<ContentInfo> getAll() {
							return Collections.singletonList(info);
						}

						public ContentInfo bestMatch() {
							return info;
						}
					};
				}
				return Results.EmptyResult;
			}
			
		};
	}

	@Test
	void testRegistryHit() {
		LicenseFinder finder = new LicenseFinder(registry, resolver);
		ContentInfo info = finder.findLicenseInformation(new MavenCoordinates("org.junit.jupiter","junit-jupiter","5.5.2").getId());
		assertEquals("EPL-2.0", info.getLicense().toString());
	}
	
	@Test
	void testResolverHit() {
		LicenseFinder finder = new LicenseFinder(registry, resolver);
		ContentInfo info = finder.findLicenseInformation(new MavenCoordinates("org.junit.jupiter","junit-jupiter","5.5.5").getId());
		assertEquals("EPL-2.0", info.getLicense().toString());
		assertNotNull(registry.find(new ContentId("maven", "mavencentral", "org.junit.jupiter","junit-jupiter","5.5.5")));
	}

}
