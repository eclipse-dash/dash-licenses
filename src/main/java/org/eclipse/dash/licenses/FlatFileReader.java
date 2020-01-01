package org.eclipse.dash.licenses;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlatFileReader implements IDependencyListReader {
	
	List<ContentIdParser> parsers = new ArrayList<>();

	private BufferedReader reader;

	public FlatFileReader(Reader input) {
		reader = new BufferedReader(input);
		parsers.add(new MavenIdParser());
		parsers.add(new PurlIdParser());
	}
	
	@Override
	public List<IContentId> iterator() {
		return reader.lines().map(line -> getContentId(line)).collect(Collectors.toList());
	}
	
	public IContentId getContentId(String value) {
		// TODO Having ContentIdParser return an Option is probably excessive.
		return parsers.stream()
			.map(parser -> parser.parseId(value))
			.filter(id -> id.isPresent())
			.findFirst()
			.orElseGet(() -> Optional.of(new InvalidContentId(value)))
			.get();
	}
}
