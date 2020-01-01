package org.eclipse.dash.bom;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.dash.licenses.LicenseSupport;
import org.junit.jupiter.api.Test;

class LicenseSupportTests {

	/**
	 * Basic test to confirm that we can look up a single license by SPDX
	 * code. Confirm that look ups are case insensitive (i.e., "epl-2.0" and
	 * "EPL-2.0" are considered equivalent).
	 */
	@Test
	void testApprovedLicense() {
		assertEquals(LicenseSupport.Status.Approved, getLicenseSupport().getStatus("EPL-2.0"));
		assertEquals(LicenseSupport.Status.Approved, getLicenseSupport().getStatus("epl-2.0"));
		assertEquals(LicenseSupport.Status.Approved, getLicenseSupport().getStatus("BSD-2-Clause"));
	}
	
	@Test
	void testRestrictedLicense() {
		assertEquals(LicenseSupport.Status.Restricted, getLicenseSupport().getStatus("GPL-2.0"));
	}
	
	/**
	 * In SPDX, an "AND" condition indicates that the consumer must accept
	 * both licenses. Generally, this sort of condition means that some of
	 * the content is under one license and some is under the other. This
	 * is complex and requires further investigation, so we expect that the 
	 * result should be "restricted".
	 */
	@Test
	void testConjunction() {
		assertEquals(LicenseSupport.Status.Restricted, getLicenseSupport().getStatus("EPL-2.0 AND GPL-2.0"));
	}
	
	/**
	 * In SPDX, an "OR" condition indicates that the consumer may accept
	 * either of the licenses (dual licensing). If all we have is ORs, then
	 * we're probably okay, and so the result should be "accepted".
	 */
	@Test
	void testDisjunction() {
		assertEquals(LicenseSupport.Status.Restricted, getLicenseSupport().getStatus("EPL-2.0 OR GPL-2.0"));
		assertEquals(LicenseSupport.Status.Restricted, getLicenseSupport().getStatus("EPL-2.0 OR MIT OR GPL-2.0"));
	}
	
	/**
	 * SPDX supports arbitrary combinations. Technically, the tested combination,
	 * "(EPL-2.0 AND MIT) OR GPL-2.0" should actually result in an "accepted"
	 * result, but since these license expressions can be arbitrarily complex,
	 * at this point-in-time, we want further investigation.
	 */
	@Test
	void testComplex() {
		// FIXME This expression should come back "approved"
		assertEquals(LicenseSupport.Status.Restricted, getLicenseSupport().getStatus("(EPL-2.0 AND MIT) OR GPL-2.0"));
	}
	
	private LicenseSupport getLicenseSupport() {
		InputStream in = getClass().getResourceAsStream("/licenses.json");
		Reader reader = new InputStreamReader(in);
		return LicenseSupport.getLicenseSupport(reader);
	}

}
