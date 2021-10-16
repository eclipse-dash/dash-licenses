package org.eclipse.dash.licenses.cli.html_parser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class JsoupProviderImpl implements JsoupProvider {

    @Override
    public Document getDocument(String url) throws IOException {
        String githubToken = System.getenv("GITHUB_TOKEN");
        Connection connection = Jsoup.connect(url).header("Authorization", "bearer " + githubToken).userAgent("Mozilla").followRedirects(true).timeout(20000);
        Connection.Response resp = connection.execute();
        if (resp.statusCode() == 200) {
            return connection.get();
        }
        return null;
    }
}
