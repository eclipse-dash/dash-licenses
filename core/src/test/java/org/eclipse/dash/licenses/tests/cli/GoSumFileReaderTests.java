package org.eclipse.dash.licenses.tests.cli;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.cli.CommandLineSettings;
import org.eclipse.dash.licenses.cli.GoSumFileReader;
import org.eclipse.dash.licenses.cli.html_parser.JsoupProvider;
import org.eclipse.dash.licenses.context.LicenseToolModule;
import org.eclipse.dash.licenses.http.HttpClientService;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

// Todo use mocks...
@ExtendWith(MockitoExtension.class)
public class GoSumFileReaderTests {

    @Mock
    private JsoupProvider jsoupProvider;
    @Mock
    private Document document;
    @Spy
    private HttpClientService httpClientService;
    @Mock
    private Consumer<InputStream> inputStreamConsumer;
    @Captor
    private ArgumentCaptor<Consumer<InputStream>> inputStreamArgumentCaptor;

    private static final String GO_SUM_FILE_CONTENT = "github.com/BurntSushi/toml v0.3.0 h1:WXkYYl6Yr3qBf1K79EBnL4mak0OimBfB0XUf9Vl28OQ=\n" +
            "github.com/BurntSushi/toml v0.3.1/go.mod h1:xHWCNGjB5oqiDr8zfno3MHue2Ht5sIBksp03qcyfWMU=\n";
//            "github.com/BurntSushi/xgb v0.0.0-20160522181843-27f122750802/go.mod h1:IVnqGOEym/WlBOVXweHU+Q+/VP0lqqI8lqeDx9IjBqo=";

    @Test // todo remove this test, when all mock test will be completed.
    public void testShouldParseGoSumFile() {
        InputStream in = new ByteArrayInputStream(GO_SUM_FILE_CONTENT.getBytes());
        CommandLineSettings settings = CommandLineSettings.getSettings(new String[]{});

        Injector injector = Guice.createInjector(new LicenseToolModule(settings));
        HttpClientService httpClientService = injector.getInstance(HttpClientService.class);
        JsoupProvider jsoupProvider = injector.getInstance(JsoupProvider.class);

        GoSumFileReader goSumFileReader = new GoSumFileReader(in, httpClientService, jsoupProvider);
        goSumFileReader.getContentIds();
    }

    @Test
    public void testShouldParseGoSumFileWithTaggedVersion() throws IOException {
        InputStream in = new ByteArrayInputStream(GO_SUM_FILE_CONTENT.getBytes());

        String githubContent = "[{\"name\":\"v0.4.1\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.4.1\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.4.1\",\"commit\":{\"sha\":\"641c3cf2148ad11ca058c000eab0453dd5d67954\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/641c3cf2148ad11ca058c000eab0453dd5d67954\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuNC4x\"},{\"name\":\"v0.4.0\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.4.0\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.4.0\",\"commit\":{\"sha\":\"642b87ad9aaf6804f7f9a3f475baf9dd362df027\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/642b87ad9aaf6804f7f9a3f475baf9dd362df027\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuNC4w\"},{\"name\":\"v0.3.1\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.3.1\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.3.1\",\"commit\":{\"sha\":\"3012a1dbe2e4bd1391d42b32f0577cb7bbc7f005\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/3012a1dbe2e4bd1391d42b32f0577cb7bbc7f005\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuMy4x\"},{\"name\":\"v0.3.0\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.3.0\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.3.0\",\"commit\":{\"sha\":\"b26d9c308763d68093482582cea63d69be07a0f0\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/b26d9c308763d68093482582cea63d69be07a0f0\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuMy4w\"},{\"name\":\"v0.2.0\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.2.0\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.2.0\",\"commit\":{\"sha\":\"bbd5bb678321a0d6e58f1099321dfa73391c1b6f\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/bbd5bb678321a0d6e58f1099321dfa73391c1b6f\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuMi4w\"},{\"name\":\"v0.1.0\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.1.0\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.1.0\",\"commit\":{\"sha\":\"2ceedfee35ad3848e49308ab0c9a4f640cfb5fb2\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/2ceedfee35ad3848e49308ab0c9a4f640cfb5fb2\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuMS4w\"}]";

        doAnswer(ans -> {
            Consumer<InputStream> callback = (Consumer<InputStream>) ans.getArguments()[2];
            InputStream stream = new ByteArrayInputStream(githubContent.getBytes(StandardCharsets.UTF_8.name()));
            callback.accept(stream);
            return null;
        }).when(httpClientService).get(anyString(), anyString(), any(Consumer.class));

        Elements elements = new Elements();
        Attributes attributes = new Attributes();
        attributes.add("content", "github.com/BurntSushi/xgb git https://github.com/BurntSushi/toml.git");
        Element element = new Element(Tag.valueOf("meta"), "", attributes);
        elements.add(element);
        when(jsoupProvider.getDocument(anyString())).thenReturn(document);
        when(document.select("meta[name='go-import']")).thenReturn(elements);

        GoSumFileReader goSumFileReader = new GoSumFileReader(in, httpClientService, jsoupProvider);
        Collection<IContentId> contentIds = goSumFileReader.getContentIds();

        assertEquals(1, contentIds.size());
        assertTrue(contentIds.stream().allMatch(id -> id.getName().equals("toml")));
        assertTrue(contentIds.stream().allMatch(id -> id.getSource().equals("github")));
        assertTrue(contentIds.stream().allMatch(id -> id.getNamespace().equals("BurntSushi")));
        assertTrue(contentIds.stream().allMatch(id -> id.getVersion().equals("3012a1dbe2e4bd1391d42b32f0577cb7bbc7f005")));
    }

    @Test
    public void shouldParseGoSumFileWithGithubHashRevision() {
        // todo
    }

    @Test
    public void shouldParseGoSumFileWithVersionSuffixInTheDeps() {
        // todo
    }

    @Test
    public void shouldFindGithubShaEvenForDependencyWithALotOfTags() {
        // todo
    }

    @Test
    public void shouldHandleUnsupportedGitProviderDependency() {
        // todo
    }
}
