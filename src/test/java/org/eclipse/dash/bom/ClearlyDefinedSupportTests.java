package org.eclipse.dash.bom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.dash.bom.clearlydefined.ClearlyDefinedSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClearlyDefinedSupportTests {
	static ClearlyDefinedSupport clearlyDefinedSupport;

	@BeforeAll
	static void setup() {
		clearlyDefinedSupport = new ClearlyDefinedSupport(CommandLineSettings.getSettings(new String[] {}));
	}

	@Test
	void testMatchAgainstClearlyDefined() {
		List<IContentData> results = new ArrayList<>();
		// FIXME Reconfigure to run against fixed data on a local test server
		// This test is running against the live server. We should either (or both)
		// refactor to allow testing against a local file, or spin up a server
		// capable of (at least) faking the interaction in a consistent manner.
		clearlyDefinedSupport.matchAgainstClearlyDefined(
			Collections.singleton(ContentId.getContentId("maven/mavencentral/io.netty/netty-transport/4.1.4")), 
			data -> results.add(data));
		assertEquals(1, results.size());
		assertEquals("maven/mavencentral/io.netty/netty-transport/4.1.4", results.get(0).getId().toString());
		assertEquals("clearlydefined", results.get(0).getAuthority());
		assertEquals(LicenseSupport.Status.Approved, results.get(0).getStatus());
	}

}
