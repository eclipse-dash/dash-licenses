/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectClasses({ BatchifierTests.class, ClearlyDefinedContentDataTests.class, ClearlyDefinedSupportTests.class,
		CommandLineSettingsTest.class, ContentIdTests.class, LicenseSupportTests.class, MavenIdParserTests.class,
		NpmJsIdParserTests.class, PackageLockFileReaderTests.class })
public class TestSuite {
}