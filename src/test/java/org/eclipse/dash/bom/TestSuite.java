package org.eclipse.dash.bom;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;

@RunWith(JUnitPlatform.class)
@SelectClasses({ 
	ClearlyDefinedContentDataTests.class, 
	ClearlyDefinedSupportTests.class,
	CommandLineSettingsTest.class, 
	ContentIdTests.class,
	LicenseSupportTests.class, 
	MavenIdParserTests.class, 
	PurlIdParserTests.class 
})
public class TestSuite {
}