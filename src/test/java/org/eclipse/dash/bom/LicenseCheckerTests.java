package org.eclipse.dash.bom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;

class LicenseCheckerTests {
	
	private LicenseChecker checker;

	@BeforeEach
	public void setup() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				Multibinder<ContentIdParser> identifierBinder = Multibinder.newSetBinder(binder(), ContentIdParser.class);
				identifierBinder.addBinding().to(MavenIdParser.class);

				Multibinder<ContentResolver> classifierBinder = Multibinder.newSetBinder(binder(), ContentResolver.class);
				classifierBinder.addBinding().to(MockResolver.class);
			}
		});
		checker = injector.getInstance(LicenseChecker.class);
	}

	@Test
	public void test() throws IOException {
		String text = 
			"p2.eclipse-plugin:org.apache.httpcomponents.httpclient:jar:4.5.6.v20190503-0009\n" + 
			"p2.eclipse-plugin:javaewah:jar:1.1.6.v20160919-1400\n" + 
			"p2.eclipse-plugin:org.eclipse.core.jobs:jar:3.8.0.v20160509-0411\n" + 
			"p2.eclipse-plugin:org.eclipse.equinox.app:jar:1.3.400.v20150715-1528\n" + 
			"pcom.jcraft:jsch:jar:0.1.55:compile\n" + 
			"com.jcraft:jzlib:jar:1.1.1:compile\n" + 
			"com.googlecode.javaewah:JavaEWAH:jar:1.1.6:compile\n" + 
			"org.slf4j:slf4j-api:jar:1.7.2:compile\n" + 
			"org.bouncycastle:bcpg-jdk15on:jar:1.61:compile\n" + 
			"org.bouncycastle:bcprov-jdk15on:jar:1.61:compile\n" + 
			"org.bouncycastle:bcpkix-jdk15on:jar:1.61:compile\n" + 
			"org.eclipse.jgit:org.eclipse.jgit:jar:5.6.0-SNAPSHOT:compile\n";
		
		Map<IContentId, IContentData> data = new HashMap<>();
		checker
			.getLicenseData(Arrays.stream(text.split("\n")).iterator(), each -> data.put(each.getId(),each));
		
		IContentData content = data.get(new ContentId("p2/orbit/p2.eclipse-plugin/org.apache.httpcomponents.httpclient/4.5.6"));
		assertEquals("p2/orbit/p2.eclipse-plugin/org.apache.httpcomponents.httpclient/4.5.6", content.getId().toString());
		assertEquals("Apache-2.0", content.getLicense());
	}

}
