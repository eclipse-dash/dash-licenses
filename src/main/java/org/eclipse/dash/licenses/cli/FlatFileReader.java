/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.dash.licenses.ContentIdParser;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.InvalidContentId;
import org.eclipse.dash.licenses.MavenIdParser;
import org.eclipse.dash.licenses.PurlIdParser;

public class FlatFileReader implements IDependencyListReader {
	
	List<ContentIdParser> parsers = new ArrayList<>();

	private BufferedReader reader;

	public FlatFileReader(Reader input) {
		reader = new BufferedReader(input);
		parsers.add(new MavenIdParser());
		parsers.add(new PurlIdParser());
	}
	
	@Override
	public List<IContentId> getContentIds() {
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
