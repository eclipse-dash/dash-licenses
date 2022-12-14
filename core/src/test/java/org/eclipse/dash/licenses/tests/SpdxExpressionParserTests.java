/*************************************************************************
 * Copyright (c) 2020,2022 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.dash.licenses.spdx.SpdxExpression;
import org.eclipse.dash.licenses.spdx.SpdxExpressionParser;
import org.eclipse.dash.licenses.spdx.SpdxIdentifier;
import org.eclipse.dash.licenses.spdx.SpdxNone;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SpdxExpressionParserTests {

	private SpdxExpression parse(String expression) {
		return new SpdxExpressionParser().parse(expression);
	}

	@Nested
	class TestParsing {

		@Test
		void testSimple1() {
			assertEquals("EPL-1.0", new SpdxExpressionParser().parse("EPL-1.0").toString());
		}

		@Test
		void testSimple2() {
			assertEquals("0BSD", new SpdxExpressionParser().parse("0BSD").toString());
		}

		@Test
		void testConjunction() {
			assertEquals("EPL-1.0 AND Apache-2.0",
					new SpdxExpressionParser().parse("EPL-1.0 AND Apache-2.0").toString());
		}

		@Test
		void testMultipleConjunction() {
			assertEquals("((EPL-1.0 AND Apache-2.0) AND MIT)",
					new SpdxExpressionParser().parse("EPL-1.0 AND Apache-2.0 AND MIT").toPrecedenceString());
		}

		@Test
		void testDisjunction() {
			assertEquals("EPL-1.0 OR Apache-2.0", new SpdxExpressionParser().parse("EPL-1.0 OR Apache-2.0").toString());
		}

		@Test
		void testPrecedence1() {
			assertEquals("(EPL-1.0 OR (Apache-2.0 AND MIT))",
					new SpdxExpressionParser().parse("EPL-1.0 OR Apache-2.0 AND MIT").toPrecedenceString());
		}

		@Test
		void testPrecedence2() {
			assertEquals("((Apache-2.0 AND MIT) OR EPL-1.0)",
					new SpdxExpressionParser().parse("Apache-2.0 AND MIT OR EPL-1.0").toPrecedenceString());
		}

		@Test
		void testPrecedence3() {
			assertEquals("((Apache-2.0 AND MIT) OR (EPL-1.0 AND BSD0))",
					new SpdxExpressionParser().parse("Apache-2.0 AND MIT OR EPL-1.0 and BSD0").toPrecedenceString());
		}

		@Test
		void testPrecedence5() {
			assertEquals("((EPL-1.0 AND Apache-2.0) OR MIT)",
					new SpdxExpressionParser().parse("EPL-1.0 AND Apache-2.0 OR MIT").toPrecedenceString());
		}

		@Test
		void testPrecedence6() {
			assertEquals("((Apache-2.0 AND (BSD0 OR MIT)) OR EPL-2.0)",
					new SpdxExpressionParser().parse("Apache-2.0 AND (BSD0 OR MIT) OR EPL-2.0").toPrecedenceString());
		}

		@Test
		void testExceptionPrecedence6() {
			assertEquals("(EPL-1.0 OR (Apache-2.0 AND MIT))",
					new SpdxExpressionParser().parse("EPL-1.0 OR Apache-2.0 AND MIT").toPrecedenceString());
		}

		@Test
		void testNestedPrecedence() {
			assertEquals("((Apache-2.0 AND MIT OR BSD0) OR EPL-1.0)",
					new SpdxExpressionParser().parse("(Apache-2.0 AND MIT OR BSD0) OR EPL-1.0").toPrecedenceString());
		}

		@Test
		void testLeadingParentheses() {
			assertEquals("((Apache-2.0 OR MIT) AND EPL-1.0)",
					new SpdxExpressionParser().parse("(Apache-2.0 OR MIT) AND EPL-1.0").toPrecedenceString());
		}

		@Test
		void testFollowingParentheses() {
			assertEquals("(MIT AND (EPL-1.0 OR Apache-2.0))",
					new SpdxExpressionParser().parse("MIT AND (EPL-1.0 OR Apache-2.0)").toPrecedenceString());
		}

		@Test
		void testNestedParentheses1() {
			assertEquals("(EPL-1.0 OR (Apache-2.0 AND (BSD0 OR MIT)))",
					new SpdxExpressionParser().parse("EPL-1.0 OR (Apache-2.0 AND (BSD0 OR MIT))").toPrecedenceString());
		}

		@Test
		void testNestedParentheses2() {
			assertEquals("(EPL-1.0 OR (Apache-2.0 AND (BSD0 OR MIT) OR EPL-2.0))",
					new SpdxExpressionParser()
							.parse("EPL-1.0 OR (Apache-2.0 AND (BSD0 OR MIT) OR EPL-2.0)")
							.toPrecedenceString());
		}

		@Test
		void testArbitraryParentheses() {
			assertEquals("((EPL-1.0 AND MPL) OR (Apache-2.0 AND (BSD0 OR MIT)))",
					new SpdxExpressionParser()
							.parse("(EPL-1.0 AND MPL) OR (Apache-2.0 AND (BSD0 OR MIT))")
							.toPrecedenceString());
		}

		@Test
		void testArbitraryParentheses2() {
			assertEquals("((EPL-1.0 AND (MPL)) OR ((Apache-2.0 AND BSD0) OR MIT))",
					new SpdxExpressionParser()
							.parse("(EPL-1.0 AND (MPL)) OR ((Apache-2.0 AND BSD0) OR MIT)")
							.toPrecedenceString());
		}

		@Test
		void testException() {
			assertEquals("EPL-1.0 WITH Exception",
					new SpdxExpressionParser().parse("EPL-1.0 with Exception").toString());
		}

		@Test
		void testExceptionPrecedence1() {
			assertEquals("(MIT OR (EPL-1.0 WITH Exception))",
					new SpdxExpressionParser().parse("MIT OR EPL-1.0 WITH Exception").toPrecedenceString());
		}

		@Test
		void testExceptionPrecedence2() {
			assertEquals("((EPL-1.0 WITH Exception) OR MIT)",
					new SpdxExpressionParser().parse("EPL-1.0 WITH Exception OR MIT").toPrecedenceString());
		}

		@Test
		void testExceptionPrecedence3() {
			assertEquals("(((EPL-1.0 AND Apache-2.0) OR MIT) OR BSD0)",
					new SpdxExpressionParser().parse("EPL-1.0 AND Apache-2.0 OR MIT OR BSD0").toPrecedenceString());
		}

		@Test
		void testExceptionPrecedence4() {
			assertEquals("(((EPL-1.0 AND Apache-2.0) OR (MIT WITH STUFF)) OR BSD0)",
					new SpdxExpressionParser()
							.parse("EPL-1.0 AND Apache-2.0 OR MIT WITH STUFF OR BSD0")
							.toPrecedenceString());
		}

		@Test
		void testPlus() {
			assertEquals("EPL-1.0+", new SpdxExpressionParser().parse("EPL-1.0+").toString());
		}

		@Test
		void testPlusInExpression1() {
			assertEquals("Apache-2.0 OR EPL-1.0+",
					new SpdxExpressionParser().parse("Apache-2.0 OR EPL-1.0+").toString());
		}

		@Test
		void testPlusInExpression2() {
			assertEquals("((BSD0 AND Apache-2.0) OR EPL-1.0+)",
					new SpdxExpressionParser().parse("BSD0 AND Apache-2.0 OR EPL-1.0+").toPrecedenceString());
		}

		@Test
		void testArbitrarilyLongExpression() {
			// Parse an expression that includes all known SPDX license identifiers
			// (as of September 2020) to ensure that we can handle 'em all.
			assertTrue(new SpdxExpressionParser()
					.parse("0BSD OR AAL OR ADSL OR AFL-1.1 OR AFL-1.2 OR AFL-2.0 OR AFL-2.1 OR AFL-3.0 OR AGPL-1.0 OR AGPL-1.0-only OR AGPL-1.0-or-later OR AGPL-3.0 OR AGPL-3.0-only OR AGPL-3.0-or-later OR AMDPLPA OR AML OR AMPAS OR ANTLR-PD OR APAFML OR APL-1.0 OR APSL-1.0 OR APSL-1.1 OR APSL-1.2 OR APSL-2.0 OR Abstyles OR Adobe-2006 OR Adobe-Glyph OR Afmparse OR Aladdin OR Apache-1.0 OR Apache-1.1 OR Apache-2.0 OR Artistic-1.0 OR Artistic-1.0-Perl OR Artistic-1.0-cl8 OR Artistic-2.0 OR BSD-1-Clause OR BSD-2-Clause OR BSD-2-Clause-FreeBSD OR BSD-2-Clause-NetBSD OR BSD-2-Clause-Patent OR BSD-2-Clause-Views OR BSD-3-Clause OR BSD-3-Clause-Attribution OR BSD-3-Clause-Clear OR BSD-3-Clause-LBNL OR BSD-3-Clause-No-Nuclear-License OR BSD-3-Clause-No-Nuclear-License-2014 OR BSD-3-Clause-No-Nuclear-Warranty OR BSD-3-Clause-Open-MPI OR BSD-4-Clause OR BSD-4-Clause-UC OR BSD-Protection OR BSD-Source-Code OR BSL-1.0 OR Bahyph OR Barr OR Beerware OR BitTorrent-1.0 OR BitTorrent-1.1 OR BlueOak-1.0.0 OR Borceux OR CAL-1.0 OR CAL-1.0-Combined-Work-Exception OR CATOSL-1.1 OR CC-BY-1.0 OR CC-BY-2.0 OR CC-BY-2.5 OR CC-BY-3.0 OR CC-BY-3.0-AT OR CC-BY-4.0 OR CC-BY-NC-1.0 OR CC-BY-NC-2.0 OR CC-BY-NC-2.5 OR CC-BY-NC-3.0 OR CC-BY-NC-4.0 OR CC-BY-NC-ND-1.0 OR CC-BY-NC-ND-2.0 OR CC-BY-NC-ND-2.5 OR CC-BY-NC-ND-3.0 OR CC-BY-NC-ND-3.0-IGO OR CC-BY-NC-ND-4.0 OR CC-BY-NC-SA-1.0 OR CC-BY-NC-SA-2.0 OR CC-BY-NC-SA-2.5 OR CC-BY-NC-SA-3.0 OR CC-BY-NC-SA-4.0 OR CC-BY-ND-1.0 OR CC-BY-ND-2.0 OR CC-BY-ND-2.5 OR CC-BY-ND-3.0 OR CC-BY-ND-4.0 OR CC-BY-SA-1.0 OR CC-BY-SA-2.0 OR CC-BY-SA-2.5 OR CC-BY-SA-3.0 OR CC-BY-SA-3.0-AT OR CC-BY-SA-4.0 OR CC-PDDC OR CC0-1.0 OR CDDL-1.0 OR CDDL-1.1 OR CDLA-Permissive-1.0 OR CDLA-Sharing-1.0 OR CECILL-1.0 OR CECILL-1.1 OR CECILL-2.0 OR CECILL-2.1 OR CECILL-B OR CECILL-C OR CERN-OHL-1.1 OR CERN-OHL-1.2 OR CERN-OHL-P-2.0 OR CERN-OHL-S-2.0 OR CERN-OHL-W-2.0 OR CNRI-Jython OR CNRI-Python OR CNRI-Python-GPL-Compatible OR CPAL-1.0 OR CPL-1.0 OR CPOL-1.02 OR CUA-OPL-1.0 OR Caldera OR ClArtistic OR Condor-1.1 OR Crossword OR CrystalStacker OR Cube OR D-FSL-1.0 OR DOC OR DSDP OR Dotseqn OR ECL-1.0 OR ECL-2.0 OR EFL-1.0 OR EFL-2.0 OR EPICS OR EPL-1.0 OR EPL-2.0 OR EUDatagrid OR EUPL-1.0 OR EUPL-1.1 OR EUPL-1.2 OR Entessa OR ErlPL-1.1 OR Eurosym OR FSFAP OR FSFUL OR FSFULLR OR FTL OR Fair OR Frameworx-1.0 OR FreeImage OR GFDL-1.1 OR GFDL-1.1-invariants-only OR GFDL-1.1-invariants-or-later OR GFDL-1.1-no-invariants-only OR GFDL-1.1-no-invariants-or-later OR GFDL-1.1-only OR GFDL-1.1-or-later OR GFDL-1.2 OR GFDL-1.2-invariants-only OR GFDL-1.2-invariants-or-later OR GFDL-1.2-no-invariants-only OR GFDL-1.2-no-invariants-or-later OR GFDL-1.2-only OR GFDL-1.2-or-later OR GFDL-1.3 OR GFDL-1.3-invariants-only OR GFDL-1.3-invariants-or-later OR GFDL-1.3-no-invariants-only OR GFDL-1.3-no-invariants-or-later OR GFDL-1.3-only OR GFDL-1.3-or-later OR GL2PS OR GLWTPL OR GPL-1.0 OR GPL-1.0+ OR GPL-1.0-only OR GPL-1.0-or-later OR GPL-2.0 OR GPL-2.0+ OR GPL-2.0-only OR GPL-2.0-or-later OR GPL-2.0-with-GCC-exception OR GPL-2.0-with-autoconf-exception OR GPL-2.0-with-bison-exception OR GPL-2.0-with-classpath-exception OR GPL-2.0-with-font-exception OR GPL-3.0 OR GPL-3.0+ OR GPL-3.0-only OR GPL-3.0-or-later OR GPL-3.0-with-GCC-exception OR GPL-3.0-with-autoconf-exception OR Giftware OR Glide OR Glulxe OR HPND OR HPND-sell-variant OR HaskellReport OR Hippocratic-2.1 OR IBM-pibs OR ICU OR IJG OR IPA OR IPL-1.0 OR ISC OR ImageMagick OR Imlib2 OR Info-ZIP OR Intel OR Intel-ACPI OR Interbase-1.0 OR JPNIC OR JSON OR JasPer-2.0 OR LAL-1.2 OR LAL-1.3 OR LGPL-2.0 OR LGPL-2.0+ OR LGPL-2.0-only OR LGPL-2.0-or-later OR LGPL-2.1 OR LGPL-2.1+ OR LGPL-2.1-only OR LGPL-2.1-or-later OR LGPL-3.0 OR LGPL-3.0+ OR LGPL-3.0-only OR LGPL-3.0-or-later OR LGPLLR OR LPL-1.0 OR LPL-1.02 OR LPPL-1.0 OR LPPL-1.1 OR LPPL-1.2 OR LPPL-1.3a OR LPPL-1.3c OR Latex2e OR Leptonica OR LiLiQ-P-1.1 OR LiLiQ-R-1.1 OR LiLiQ-Rplus-1.1 OR Libpng OR Linux-OpenIB OR MIT OR MIT-0 OR MIT-CMU OR MIT-advertising OR MIT-enna OR MIT-feh OR MITNFA OR MPL-1.0 OR MPL-1.1 OR MPL-2.0 OR MPL-2.0-no-copyleft-exception OR MS-PL OR MS-RL OR MTLL OR MakeIndex OR MirOS OR Motosoto OR MulanPSL-1.0 OR MulanPSL-2.0 OR Multics OR Mup OR NASA-1.3 OR NBPL-1.0 OR NCGL-UK-2.0 OR NCSA OR NGPL OR NIST-PD OR NIST-PD-fallback OR NLOD-1.0 OR NLPL OR NOSL OR NPL-1.0 OR NPL-1.1 OR NPOSL-3.0 OR NRL OR NTP OR NTP-0 OR Naumen OR Net-SNMP OR NetCDF OR Newsletr OR Nokia OR Noweb OR Nunit OR O-UDA-1.0 OR OCCT-PL OR OCLC-2.0 OR ODC-By-1.0 OR ODbL-1.0 OR OFL-1.0 OR OFL-1.0-RFN OR OFL-1.0-no-RFN OR OFL-1.1 OR OFL-1.1-RFN OR OFL-1.1-no-RFN OR OGC-1.0 OR OGL-Canada-2.0 OR OGL-UK-1.0 OR OGL-UK-2.0 OR OGL-UK-3.0 OR OGTSL OR OLDAP-1.1 OR OLDAP-1.2 OR OLDAP-1.3 OR OLDAP-1.4 OR OLDAP-2.0 OR OLDAP-2.0.1 OR OLDAP-2.1 OR OLDAP-2.2 OR OLDAP-2.2.1 OR OLDAP-2.2.2 OR OLDAP-2.3 OR OLDAP-2.4 OR OLDAP-2.5 OR OLDAP-2.6 OR OLDAP-2.7 OR OLDAP-2.8 OR OML OR OPL-1.0 OR OSET-PL-2.1 OR OSL-1.0 OR OSL-1.1 OR OSL-2.0 OR OSL-2.1 OR OSL-3.0 OR OpenSSL OR PDDL-1.0 OR PHP-3.0 OR PHP-3.01 OR PSF-2.0 OR Parity-6.0.0 OR Parity-7.0.0 OR Plexus OR PolyForm-Noncommercial-1.0.0 OR PolyForm-Small-Business-1.0.0 OR PostgreSQL OR Python-2.0 OR QPL-1.0 OR Qhull OR RHeCos-1.1 OR RPL-1.1 OR RPL-1.5 OR RPSL-1.0 OR RSA-MD OR RSCPL OR Rdisc OR Ruby OR SAX-PD OR SCEA OR SGI-B-1.0 OR SGI-B-1.1 OR SGI-B-2.0 OR SHL-0.5 OR SHL-0.51 OR SISSL OR SISSL-1.2 OR SMLNJ OR SMPPL OR SNIA OR SPL-1.0 OR SSH-OpenSSH OR SSH-short OR SSPL-1.0 OR SWL OR Saxpath OR Sendmail OR Sendmail-8.23 OR SimPL-2.0 OR Sleepycat OR Spencer-86 OR Spencer-94 OR Spencer-99 OR StandardML-NJ OR SugarCRM-1.1.3 OR TAPR-OHL-1.0 OR TCL OR TCP-wrappers OR TMate OR TORQUE-1.1 OR TOSL OR TU-Berlin-1.0 OR TU-Berlin-2.0 OR UCL-1.0 OR UPL-1.0 OR Unicode-DFS-2015 OR Unicode-DFS-2016 OR Unicode-TOU OR Unlicense OR VOSTROM OR VSL-1.0 OR Vim OR W3C OR W3C-19980720 OR W3C-20150513 OR WTFPL OR Watcom-1.0 OR Wsuipa OR X11 OR XFree86-1.1 OR XSkat OR Xerox OR Xnet OR YPL-1.0 OR YPL-1.1 OR ZPL-1.1 OR ZPL-2.0 OR ZPL-2.1 OR Zed OR Zend-2.0 OR Zimbra-1.3 OR Zimbra-1.4 OR Zlib OR blessing OR bzip2-1.0.5 OR bzip2-1.0.6 OR copyleft-next-0.3.0 OR copyleft-next-0.3.1 OR curl OR diffmark OR dvipdfm OR eCos-2.0 OR eGenix OR etalab-2.0 OR gSOAP-1.3b OR gnuplot OR iMatix OR libpng-2.0 OR libselinux-1.0 OR libtiff OR mpich2 OR psfrag OR psutils OR wxWindows OR xinetd OR xpp OR zlib-acknowledgement")
					.isValid());
		}

		@Test
		void testEqualsExpression1() {
			var parser = new SpdxExpressionParser();
			assertEquals(parser.parse("EPL-2.0 and Apache-2.0"), parser.parse("Apache-2.0 and EPL-2.0"));
		}

		@Test
		void testEqualsExpression2() {
			var parser = new SpdxExpressionParser();
			assertEquals(parser.parse("EPL-2.0 and Apache-2.0 or MIT"), parser.parse("MIT OR Apache-2.0 and EPL-2.0"));
		}

		@Disabled
		@Test
		void testEqualsExpression3() {
			var parser = new SpdxExpressionParser();
			assertEquals(parser.parse("EPL-2.0 and Apache-2.0 AND MIT"),
					parser.parse("MIT AND Apache-2.0 and EPL-2.0"));
		}

		@Test
		void testNullExpression() {
			var parser = new SpdxExpressionParser();
			assertEquals(parser.parse("NONE and EPL-2.0"), parser.parse("EPL-2.0"));
		}

		@Test
		void testGrouping1() {
			assertEquals("EPL-2.0 AND Apache-2.0 AND LGPL-2.1-or-later",
					SpdxIdentifier
							.code("EPL-2.0")
							.and(SpdxIdentifier.code("Apache-2.0"))
							.and(SpdxIdentifier.code("LGPL-2.1-or-later"))
							.toString());
		}

		@Test
		void testGrouping2() {
			assertEquals("EPL-2.0 AND (Apache-2.0 OR LGPL-2.1-or-later)",
					SpdxIdentifier
							.code("EPL-2.0")
							.and(new SpdxExpressionParser().parse("Apache-2.0 OR LGPL-2.1-or-later"))
							.toString());
		}

		@Test
		void testGrouping3() {
			assertEquals("EPL-2.0", SpdxNone.INSTANCE.and(SpdxIdentifier.code("EPL-2.0")).toString());
		}
	}

	@Nested
	class TestBroken {
		@Test
		void testBrokenBinaryExpression1() {
			assertFalse(new SpdxExpressionParser().parse("BSD0 AND ").isValid());
		}

		@Test
		void testBrokenBinaryExpression2() {
			assertFalse(new SpdxExpressionParser().parse("AND BSD0").isValid());
		}

		@Test
		void testBrokenPlus() {
			assertFalse(new SpdxExpressionParser().parse("+BSD0").isValid());
		}

		@Test
		void testMultiplePlus() {
			assertFalse(new SpdxExpressionParser().parse("BSD0++").isValid());
		}

		@Test
		void testBogusPlus() {
			assertFalse(new SpdxExpressionParser().parse("(EPL-2.0 AND Apache-2.0)+").isValid());
		}

		@Test
		void testNonsense() {
			assertFalse(new SpdxExpressionParser().parse("BSD0 BSD0").isValid());
		}

		@Test
		void testEOL() {
			assertFalse(new SpdxExpressionParser().parse("(EPL-2.0 AND Apache-2.0").isValid());
		}
	}

	@Nested
	class TestMatching {
		@Test
		void testMatchSimple() {
			assertTrue(new SpdxExpressionParser().parse("EPL-2.0").matchesApproved(Collections.singleton("EPL-2.0")));
		}

		@Test
		void testMatchConjunction() {
			Set<String> approved = new HashSet<>();
			approved.add("EPL-2.0");
			approved.add("Apache-2.0");

			assertTrue(new SpdxExpressionParser().parse("EPL-2.0 AND Apache-2.0").matchesApproved(approved));
		}

		@Test
		void testMatchConjunctionFail() {
			Set<String> approved = new HashSet<>();
			approved.add("EPL-2.0");
			approved.add("Apache-2.0");

			assertFalse(new SpdxExpressionParser().parse("EPL-2.0 AND BSD0").matchesApproved(approved));
		}

		@Test
		void testMatchDisjunction() {
			Set<String> approved = new HashSet<>();
			approved.add("EPL-2.0");
			approved.add("Apache-2.0");

			assertTrue(new SpdxExpressionParser().parse("EPL-2.0 OR BSD0").matchesApproved(approved));
		}

		@Test
		void testMatchArbitrary() {
			Set<String> approved = new HashSet<>();
			approved.add("EPL-2.0");
			approved.add("Apache-2.0");

			assertTrue(new SpdxExpressionParser().parse("EPL-2.0 AND (Apache-2.0 OR BSD0)").matchesApproved(approved));
		}

		@Test
		void testMatchPlus1() {
			Set<String> approved = new HashSet<>();
			approved.add("EPL-1.0");

			assertTrue(new SpdxExpressionParser().parse("EPL-1.0+").matchesApproved(approved));
		}

		@Disabled
		@Test
		void testMatchPlus2() {
			Set<String> approved = new HashSet<>();
			approved.add("EPL-2.0");

			assertTrue(new SpdxExpressionParser().parse("EPL-1.0+").matchesApproved(approved));
		}
	}

	@Nested
	class TestCollapsing {
		@Test
		void testSimpleCollapse0() {
			var expression = parse("EPL-2.0");
			assertEquals("EPL-2.0", expression.collapse().toString());
		}

		@Test
		void testSimpleCollapse1() {
			var expression = parse("EPL-2.0 AND EPL-2.0");
			assertEquals("EPL-2.0", expression.collapse().toString());
		}

		@Test
		void testSimpleCollapse2() {
			var expression = parse("EPL-2.0 AND EPL-2.0 AND EPL-2.0");
			assertEquals("EPL-2.0", expression.collapse().toString());
		}

		@Test
		void testCollapse1() {
			var expression = parse("EPL-2.0 AND Apache-2.0 AND EPL-2.0");
			assertEquals("EPL-2.0 AND Apache-2.0", expression.collapse().toString());
		}

		@Test
		void testCollapse2() {
			var expression = parse("EPL-2.0 AND Apache-2.0 AND EPL-2.0 AND Apache-2.0 AND EPL-2.0");
			assertEquals("EPL-2.0 AND Apache-2.0", expression.collapse().toString());
		}

		@Test
		void testCollapse3() {
			var expression = parse("EPL-2.0 AND Apache-2.0 AND Apache-2.0 AND Apache-2.0 AND EPL-2.0");
			assertEquals("EPL-2.0 AND Apache-2.0", expression.collapse().toString());
		}

		@Disabled
		@Test
		void testCollapse4() {
			var expression = new SpdxExpressionParser()
					.parse("EPL-2.0 AND Apache-2.0 AND (EPL-2.0 OR Apache-2.0) AND EPL-2.0");
			assertEquals("EPL-2.0 AND Apache-2.0", expression.collapse().toString());
		}

		@Disabled
		@Test
		void testCollapse5() {
			var expression = new SpdxExpressionParser().parse("(EPL-2.0 AND Apache-2.0) AND (EPL-2.0 OR Apache-2.0)");
			assertEquals("EPL-2.0 AND Apache-2.0", expression.collapse().toString());
		}
	}

	@Nested
	class TestPrinting {
		@Test
		void testAnnotated1() {
			var expression = new SpdxExpressionParser().parse("EPL-2.0 AND (Apache-2.0 OR 0BSD+)");
			var output = expression.toAnnotatedString(identifier -> "**" + identifier + "**");
			assertEquals("**EPL-2.0** AND (**Apache-2.0** OR **0BSD**+)", output);
		}

		@Test
		void testAnnotated2() {
			var expression = new SpdxExpressionParser().parse("GPL-2.0-only WITH Classpath-exception-2.0");
			var output = expression
					.toAnnotatedString(
							identifier -> "[" + identifier + "](https://spdx.org/licenses/" + identifier + ".html)");
			assertEquals(
					"[GPL-2.0-only](https://spdx.org/licenses/GPL-2.0-only.html) WITH [Classpath-exception-2.0](https://spdx.org/licenses/Classpath-exception-2.0.html)",
					output);
		}
	}
}
