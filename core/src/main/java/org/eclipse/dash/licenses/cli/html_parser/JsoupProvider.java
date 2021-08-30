package org.eclipse.dash.licenses.cli.html_parser;

import org.jsoup.nodes.Document;

import java.io.IOException;

public interface JsoupProvider {
   Document getDocument(String url) throws IOException;
}
