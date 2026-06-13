/*************************************************************************
 * Copyright (c) 2026 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.dash.licenses.cli.SbomFileReader;
import org.junit.jupiter.api.Test;

class SbomFileReaderTests {

	private static final String AFS_CYCLONEDX_JSON = "/afs-1.0.0-cyclonedx.json";

	@Test
	void testV1Format() throws Exception {
		var input = new File(this.getClass().getResource(AFS_CYCLONEDX_JSON).toURI());
		SbomFileReader reader = new SbomFileReader(input);
		var expected = Arrays.asList(new String[] { 
				"maven/mavencentral/org.eclipse.serializer/afs/1.0.0",
				"maven/mavencentral/org.eclipse.serializer/base/1.0.0",
				"maven/mavencentral/org.slf4j/slf4j-api/1.7.32" 
		});
		var found = reader.getContentIds().stream().map(each -> each.toString()).collect(Collectors.toList());
		assertEquals(expected, found);
	}
}
