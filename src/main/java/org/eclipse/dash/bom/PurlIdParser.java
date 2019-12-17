package org.eclipse.dash.bom;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PurlIdParser implements ContentIdParser {
	
	private static Pattern purlPattern = Pattern
		.compile("\\s*"
				+ "(?<namespace>@\\S+\\/)?"
				+ "(?<name>\\S+)"
				+ "@(?<version>\\d+(?:\\.\\d+){0,2})"
				+ "\\s*");

	@Override
	public Optional<IContentId> parseId(String value) {
		Matcher matcher = purlPattern.matcher(value);
		if (!matcher.matches()) return Optional.empty();

		String namespace = matcher.group("namespace");
		if (namespace == null) namespace = "-";
		String name = matcher.group("name");
		String version = matcher.group("version");
		
		return Optional.of(new ContentId("npm", "npmjs", namespace, name, version));
	}
}
