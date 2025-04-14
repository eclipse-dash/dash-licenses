/*************************************************************************
 * Copyright (c) 2025 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.dash.licenses.util.GitUtils;
import org.junit.jupiter.api.Test;

class GitUtilsTests {
	@Test
	void testCorrectlyIdentifiesEclipseReport() {
		assertEquals("https://github.com/eclipse-dash/dash-licenses.git", GitUtils.getEclipseRemote());
	}
}
