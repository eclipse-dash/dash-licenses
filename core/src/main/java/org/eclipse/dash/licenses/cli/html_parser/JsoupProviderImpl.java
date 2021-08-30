package org.eclipse.dash.licenses.cli.html_parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class JsoupProviderImpl implements JsoupProvider {

    @Override
    public Document getDocument(String url) throws IOException {
        return Jsoup.connect(url).get();
    }
}
