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
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GoSumFileReaderTests {

    @Mock
    private JsoupProvider jsoupProvider;
    @Mock
    private Document document;
    @Spy
    private HttpClientService httpClientService;

    @Test // Todo remove this test, when all mock tests will be completed.
    public void testShouldParseGoSumFileWithIncompatibleSuffix0() {
        String GO_SUM_FILE_CONTENT = "gotest.tools/v3 v3.0.2/go.mod h1:3SzNCllyD9/Y+b5r9JIKQ474KzkZyqLqEfYqMsX94Bk=\n" +
                "gotest.tools/v3 v3.0.3/go.mod h1:Z7Lb0S5l+klDB31fvDQX8ss/FlKDxtlFlw3Oa8Ymbl8=";
        InputStream in = new ByteArrayInputStream(GO_SUM_FILE_CONTENT.getBytes());
        CommandLineSettings settings = CommandLineSettings.getSettings(new String[]{});

        Injector injector = Guice.createInjector(new LicenseToolModule(settings));
        HttpClientService httpClientService = injector.getInstance(HttpClientService.class);
        JsoupProvider jsoupProvider = injector.getInstance(JsoupProvider.class);

        GoSumFileReader goSumFileReader = new GoSumFileReader(in, httpClientService, jsoupProvider);
        Collection<IContentId> contentIds = goSumFileReader.getContentIds();

        assertEquals(1, contentIds.size());
        assertTrue(contentIds.stream().allMatch(id -> id.getName().equals("gotest.tools")));
        assertTrue(contentIds.stream().allMatch(id -> id.getSource().equals("github")));
        assertTrue(contentIds.stream().allMatch(id -> id.getNamespace().equals("gotestyourself")));
        assertTrue(contentIds.stream().allMatch(id -> id.getVersion().equals("568bc57cc5c19a2ef85e5749870b49a4cc2ab54d")));
    }

    @Test
    public void testShouldParseGoSumFileWithIncompatibleSuffix() throws IOException {
        String GO_SUM_FILE_CONTENT = "gotest.tools v2.2.0+incompatible h1:VsBPFP1AI068pPrMxtb/S8Zkgf9xEmTLJjfM+P5UIEo=";
        InputStream in = new ByteArrayInputStream(GO_SUM_FILE_CONTENT.getBytes());

        String githubContent = "[{\"name\": \"v2.2.0\", \"commit\": {\"sha\":\"7c797b5133e5460410dbb22ba779bf35e6975dea\"}}]";

        doAnswer(ans -> {
            Consumer<InputStream> callback = (Consumer<InputStream>) ans.getArguments()[3];
            InputStream stream = new ByteArrayInputStream(githubContent.getBytes(StandardCharsets.UTF_8.name()));
            callback.accept(stream);
            return null;
        }).when(httpClientService).get(anyString(), anyString(), any(Map.class), any(Consumer.class));

        Elements elements = new Elements();
        Attributes attributes = new Attributes();
        attributes.add("content", "gotest.tools git https://github.com/gotestyourself/gotest.tools.git");
        Element element = new Element(Tag.valueOf("meta"), "", attributes);
        elements.add(element);
        when(jsoupProvider.getDocument(anyString())).thenReturn(document);
        when(document.select("meta[name='go-import']")).thenReturn(elements);

        GoSumFileReader goSumFileReader = new GoSumFileReader(in, httpClientService, jsoupProvider);
        Collection<IContentId> contentIds = goSumFileReader.getContentIds();

        assertEquals(1, contentIds.size());
        assertTrue(contentIds.stream().allMatch(id -> id.getName().equals("gotest.tools")));
        assertTrue(contentIds.stream().allMatch(id -> id.getSource().equals("github")));
        assertTrue(contentIds.stream().allMatch(id -> id.getNamespace().equals("gotestyourself")));
        assertTrue(contentIds.stream().allMatch(id -> id.getVersion().equals("7c797b5133e5460410dbb22ba779bf35e6975dea")));
    }

    @Test // Todo remove this test, when all mock tests will be completed.
    public void testShouldParseGoSumFileWithTaggedVersion0() {
        String GO_SUM_FILE_CONTENT = "github.com/BurntSushi/toml v0.3.0 h1:WXkYYl6Yr3qBf1K79EBnL4mak0OimBfB0XUf9Vl28OQ=\n" +
                "github.com/BurntSushi/toml v0.3.1/go.mod h1:xHWCNGjB5oqiDr8zfno3MHue2Ht5sIBksp03qcyfWMU=\n";
        InputStream in = new ByteArrayInputStream(GO_SUM_FILE_CONTENT.getBytes());
        CommandLineSettings settings = CommandLineSettings.getSettings(new String[]{});

        Injector injector = Guice.createInjector(new LicenseToolModule(settings));
        HttpClientService httpClientService = injector.getInstance(HttpClientService.class);
        JsoupProvider jsoupProvider = injector.getInstance(JsoupProvider.class);

        GoSumFileReader goSumFileReader = new GoSumFileReader(in, httpClientService, jsoupProvider);
        Collection<IContentId> contentIds = goSumFileReader.getContentIds();

        assertEquals(1, contentIds.size());
        assertTrue(contentIds.stream().allMatch(id -> id.getName().equals("toml")));
        assertTrue(contentIds.stream().allMatch(id -> id.getSource().equals("github")));
        assertTrue(contentIds.stream().allMatch(id -> id.getNamespace().equals("BurntSushi")));
        assertTrue(contentIds.stream().allMatch(id -> id.getVersion().equals("3012a1dbe2e4bd1391d42b32f0577cb7bbc7f005")));
    }

    @Test
    public void testShouldParseGoSumFileWithTaggedVersion() throws IOException {
        String GO_SUM_FILE_CONTENT = "github.com/BurntSushi/toml v0.3.0 h1:WXkYYl6Yr3qBf1K79EBnL4mak0OimBfB0XUf9Vl28OQ=\n" +
                "github.com/BurntSushi/toml v0.3.1/go.mod h1:xHWCNGjB5oqiDr8zfno3MHue2Ht5sIBksp03qcyfWMU=\n";
        InputStream in = new ByteArrayInputStream(GO_SUM_FILE_CONTENT.getBytes());

        String githubContent = "[{\"name\":\"v0.4.1\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.4.1\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.4.1\",\"commit\":{\"sha\":\"641c3cf2148ad11ca058c000eab0453dd5d67954\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/641c3cf2148ad11ca058c000eab0453dd5d67954\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuNC4x\"},{\"name\":\"v0.4.0\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.4.0\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.4.0\",\"commit\":{\"sha\":\"642b87ad9aaf6804f7f9a3f475baf9dd362df027\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/642b87ad9aaf6804f7f9a3f475baf9dd362df027\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuNC4w\"},{\"name\":\"v0.3.1\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.3.1\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.3.1\",\"commit\":{\"sha\":\"3012a1dbe2e4bd1391d42b32f0577cb7bbc7f005\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/3012a1dbe2e4bd1391d42b32f0577cb7bbc7f005\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuMy4x\"},{\"name\":\"v0.3.0\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.3.0\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.3.0\",\"commit\":{\"sha\":\"b26d9c308763d68093482582cea63d69be07a0f0\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/b26d9c308763d68093482582cea63d69be07a0f0\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuMy4w\"},{\"name\":\"v0.2.0\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.2.0\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.2.0\",\"commit\":{\"sha\":\"bbd5bb678321a0d6e58f1099321dfa73391c1b6f\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/bbd5bb678321a0d6e58f1099321dfa73391c1b6f\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuMi4w\"},{\"name\":\"v0.1.0\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.1.0\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.1.0\",\"commit\":{\"sha\":\"2ceedfee35ad3848e49308ab0c9a4f640cfb5fb2\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/2ceedfee35ad3848e49308ab0c9a4f640cfb5fb2\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuMS4w\"}]";

        doAnswer(ans -> {
            Consumer<InputStream> callback = (Consumer<InputStream>) ans.getArguments()[3];
            InputStream stream = new ByteArrayInputStream(githubContent.getBytes(StandardCharsets.UTF_8.name()));
            callback.accept(stream);
            return null;
        }).when(httpClientService).get(anyString(), anyString(), any(Map.class), any(Consumer.class));

        Elements elements = new Elements();
        Attributes attributes = new Attributes();
        attributes.add("content", "github.com/BurntSushi/toml git https://github.com/BurntSushi/toml.git");
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

    // Todo remove this test, when all mock tests will be completed.
    @Test
    public void shouldParseGoSumFileWithGithubHashRevision0() {
        String GO_SUM_FILE_CONTENT = "github.com/BurntSushi/xgb v0.0.0-20160522181843-27f122750802/go.mod h1:IVnqGOEym/WlBOVXweHU+Q+/VP0lqqI8lqeDx9IjBqo=";
        InputStream in = new ByteArrayInputStream(GO_SUM_FILE_CONTENT.getBytes());
        CommandLineSettings settings = CommandLineSettings.getSettings(new String[]{});

        Injector injector = Guice.createInjector(new LicenseToolModule(settings));
        HttpClientService httpClientService = injector.getInstance(HttpClientService.class);
        JsoupProvider jsoupProvider = injector.getInstance(JsoupProvider.class);

        GoSumFileReader goSumFileReader = new GoSumFileReader(in, httpClientService, jsoupProvider);
        Collection<IContentId> contentIds = goSumFileReader.getContentIds();

        assertEquals(1, contentIds.size());
        assertTrue(contentIds.stream().allMatch(id -> id.getName().equals("xgb")));
        assertTrue(contentIds.stream().allMatch(id -> id.getSource().equals("github")));
        assertTrue(contentIds.stream().allMatch(id -> id.getNamespace().equals("BurntSushi")));
        assertTrue(contentIds.stream().allMatch(id -> id.getVersion().equals("27f122750802c950b2c869a5b63dafcf590ced95")));
    }

    @Test
    public void shouldParseGoSumFileWithGithubHashRevision() throws IOException {
        String GO_SUM_FILE_CONTENT = "github.com/BurntSushi/xgb v0.0.0-20160522181843-27f122750802/go.mod h1:IVnqGOEym/WlBOVXweHU+Q+/VP0lqqI8lqeDx9IjBqo=";
        InputStream in = new ByteArrayInputStream(GO_SUM_FILE_CONTENT.getBytes());

        String githubContent = "{\"sha\":\"27f122750802c950b2c869a5b63dafcf590ced95\"}";

        doAnswer(ans -> {
            Consumer<InputStream> callback = (Consumer<InputStream>) ans.getArguments()[3];
            InputStream stream = new ByteArrayInputStream(githubContent.getBytes(StandardCharsets.UTF_8.name()));
            callback.accept(stream);
            return null;
        }).when(httpClientService).get(anyString(), anyString(), any(Map.class), any(Consumer.class));

        Elements elements = new Elements();
        Attributes attributes = new Attributes();
        attributes.add("content", "github.com/BurntSushi/xgb git https://github.com/BurntSushi/xgb.git");
        Element element = new Element(Tag.valueOf("meta"), "", attributes);
        elements.add(element);
        when(jsoupProvider.getDocument(anyString())).thenReturn(document);
        when(document.select("meta[name='go-import']")).thenReturn(elements);

        GoSumFileReader goSumFileReader = new GoSumFileReader(in, httpClientService, jsoupProvider);
        Collection<IContentId> contentIds = goSumFileReader.getContentIds();

        assertEquals(1, contentIds.size());
        assertTrue(contentIds.stream().allMatch(id -> id.getName().equals("xgb")));
        assertTrue(contentIds.stream().allMatch(id -> id.getSource().equals("github")));
        assertTrue(contentIds.stream().allMatch(id -> id.getNamespace().equals("BurntSushi")));
        assertTrue(contentIds.stream().allMatch(id -> id.getVersion().equals("27f122750802c950b2c869a5b63dafcf590ced95")));
    }
  
    // Todo remove this test, when all mock tests will be completed.
    @Test
    public void shouldParseGoSumFileWithVersionSuffixInTheDeps0() {
        String GO_SUM_FILE_CONTENT = "gomodules.xyz/jsonpatch/v2 v2.0.1/go.mod h1:IhYNNY4jnS53ZnfE4PAmpKtDpTCj1JFXc+3mwe7XcUU=";
        InputStream in = new ByteArrayInputStream(GO_SUM_FILE_CONTENT.getBytes());
        CommandLineSettings settings = CommandLineSettings.getSettings(new String[]{});

        Injector injector = Guice.createInjector(new LicenseToolModule(settings));
        HttpClientService httpClientService = injector.getInstance(HttpClientService.class);
        JsoupProvider jsoupProvider = injector.getInstance(JsoupProvider.class);

        GoSumFileReader goSumFileReader = new GoSumFileReader(in, httpClientService, jsoupProvider);
        Collection<IContentId> contentIds = goSumFileReader.getContentIds();

        assertEquals(1, contentIds.size());
        assertTrue(contentIds.stream().allMatch(id -> id.getName().equals("jsonpatch")));
        assertTrue(contentIds.stream().allMatch(id -> id.getSource().equals("github")));
        assertTrue(contentIds.stream().allMatch(id -> id.getNamespace().equals("gomodules")));
        assertTrue(contentIds.stream().allMatch(id -> id.getVersion().equals("e8422f09d27ee2c8cfb2c7f8089eb9eeb0764849")));
    }

    // Advanced case: some golang dependencies has some "version suffix" to split high versions. For example v2, v3, v4.
    // This suffix is applied after dependency name and can be defined with regexp "/v[0-9]+".
    // Notice: "version suffix" that's not official term, we use it to simplify explanation.
    @Test
    public void shouldParseGoSumFileWithVersionSuffixInTheDeps() throws IOException {
        String GO_SUM_FILE_CONTENT = "gomodules.xyz/jsonpatch/v2 v2.0.1/go.mod h1:IhYNNY4jnS53ZnfE4PAmpKtDpTCj1JFXc+3mwe7XcUU=";
        InputStream in = new ByteArrayInputStream(GO_SUM_FILE_CONTENT.getBytes());

        String githubContent = "[{\"name\":\"v3.0.1\",\"zipball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/zipball/refs/tags/v3.0.1\",\"tarball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/tarball/refs/tags/v3.0.1\",\"commit\":{\"sha\":\"e2d6410c0299d49b797482dc765a3b49ecc8a4b6\",\"url\":\"https://api.github.com/repos/gomodules/jsonpatch/commits/e2d6410c0299d49b797482dc765a3b49ecc8a4b6\"},\"node_id\":\"MDM6UmVmMTIzNTU4OTk0OnJlZnMvdGFncy92My4wLjE=\"},{\"name\":\"v3.0.0\",\"zipball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/zipball/refs/tags/v3.0.0\",\"tarball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/tarball/refs/tags/v3.0.0\",\"commit\":{\"sha\":\"e679f7947f2ddb72963b6ce4593c254ba9db06d7\",\"url\":\"https://api.github.com/repos/gomodules/jsonpatch/commits/e679f7947f2ddb72963b6ce4593c254ba9db06d7\"},\"node_id\":\"MDM6UmVmMTIzNTU4OTk0OnJlZnMvdGFncy92My4wLjA=\"},{\"name\":\"v2.2.0\",\"zipball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/zipball/refs/tags/v2.2.0\",\"tarball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/tarball/refs/tags/v2.2.0\",\"commit\":{\"sha\":\"93a4dafa4292a7e6cfff7d5d9aefc0f48219f07d\",\"url\":\"https://api.github.com/repos/gomodules/jsonpatch/commits/93a4dafa4292a7e6cfff7d5d9aefc0f48219f07d\"},\"node_id\":\"MDM6UmVmMTIzNTU4OTk0OnJlZnMvdGFncy92Mi4yLjA=\"},{\"name\":\"v2.1.0\",\"zipball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/zipball/refs/tags/v2.1.0\",\"tarball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/tarball/refs/tags/v2.1.0\",\"commit\":{\"sha\":\"1c2a0262323edc27502942d9727c95646b1c3cbe\",\"url\":\"https://api.github.com/repos/gomodules/jsonpatch/commits/1c2a0262323edc27502942d9727c95646b1c3cbe\"},\"node_id\":\"MDM6UmVmMTIzNTU4OTk0OnJlZnMvdGFncy92Mi4xLjA=\"},{\"name\":\"v2.0.1\",\"zipball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/zipball/refs/tags/v2.0.1\",\"tarball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/tarball/refs/tags/v2.0.1\",\"commit\":{\"sha\":\"e8422f09d27ee2c8cfb2c7f8089eb9eeb0764849\",\"url\":\"https://api.github.com/repos/gomodules/jsonpatch/commits/e8422f09d27ee2c8cfb2c7f8089eb9eeb0764849\"},\"node_id\":\"MDM6UmVmMTIzNTU4OTk0OnJlZnMvdGFncy92Mi4wLjE=\"},{\"name\":\"v2.0.0\",\"zipball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/zipball/refs/tags/v2.0.0\",\"tarball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/tarball/refs/tags/v2.0.0\",\"commit\":{\"sha\":\"e8422f09d27ee2c8cfb2c7f8089eb9eeb0764849\",\"url\":\"https://api.github.com/repos/gomodules/jsonpatch/commits/e8422f09d27ee2c8cfb2c7f8089eb9eeb0764849\"},\"node_id\":\"MDM6UmVmMTIzNTU4OTk0OnJlZnMvdGFncy92Mi4wLjA=\"},{\"name\":\"v1.0.1\",\"zipball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/zipball/refs/tags/v1.0.1\",\"tarball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/tarball/refs/tags/v1.0.1\",\"commit\":{\"sha\":\"7f760186a63d02954613b9b22de68a5b2cb2b1bf\",\"url\":\"https://api.github.com/repos/gomodules/jsonpatch/commits/7f760186a63d02954613b9b22de68a5b2cb2b1bf\"},\"node_id\":\"MDM6UmVmMTIzNTU4OTk0OnJlZnMvdGFncy92MS4wLjE=\"},{\"name\":\"1.0.0\",\"zipball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/zipball/refs/tags/1.0.0\",\"tarball_url\":\"https://api.github.com/repos/gomodules/jsonpatch/tarball/refs/tags/1.0.0\",\"commit\":{\"sha\":\"7c0e3b262f30165a8ec3d0b4c6059fd92703bfb2\",\"url\":\"https://api.github.com/repos/gomodules/jsonpatch/commits/7c0e3b262f30165a8ec3d0b4c6059fd92703bfb2\"},\"node_id\":\"MDM6UmVmMTIzNTU4OTk0OnJlZnMvdGFncy8xLjAuMA==\"}]\n";

        doAnswer(ans -> {
            Consumer<InputStream> callback = (Consumer<InputStream>) ans.getArguments()[3];
            InputStream stream = new ByteArrayInputStream(githubContent.getBytes(StandardCharsets.UTF_8.name()));
            callback.accept(stream);
            return null;
        }).when(httpClientService).get(anyString(), anyString(), any(Map.class), any(Consumer.class));

        Elements elements = new Elements();
        Attributes attributes = new Attributes();
        attributes.add("content", "gomodules.xyz/jsonpatch git https://github.com/gomodules/jsonpatch");
        Element element = new Element(Tag.valueOf("meta"), "", attributes);
        elements.add(element);
        when(jsoupProvider.getDocument(anyString())).thenReturn(document);
        when(document.select("meta[name='go-import']")).thenReturn(elements);

        GoSumFileReader goSumFileReader = new GoSumFileReader(in, httpClientService, jsoupProvider);
        Collection<IContentId> contentIds = goSumFileReader.getContentIds();

        assertEquals(1, contentIds.size());
        assertTrue(contentIds.stream().allMatch(id -> id.getName().equals("jsonpatch")));
        assertTrue(contentIds.stream().allMatch(id -> id.getSource().equals("github")));
        assertTrue(contentIds.stream().allMatch(id -> id.getNamespace().equals("gomodules")));
        assertTrue(contentIds.stream().allMatch(id -> id.getVersion().equals("e8422f09d27ee2c8cfb2c7f8089eb9eeb0764849")));
    }

    @Test
    public void shouldFindGithubSHAEvenForDependencyWithALotOfTags() throws IOException {
        // todo
    }

    @Test
    public void shouldHandleUnsupportedGitProviderDependency() {
        // todo
    }

    // Todo remove this test, when all mock tests will be completed.
    @Test
    public void shouldHandleDependencyFromMultiPackageGithubRepo0() {
        // We have dependency - golang sub-module "api"
        String goSumFileWithSubModuleReference = "sigs.k8s.io/kustomize/api v0.8.8/go.mod h1:He1zoK0nk43Pc6NlV085xDXDXTNprtcyKZVm3swsdNY=";
        InputStream in = new ByteArrayInputStream(goSumFileWithSubModuleReference.getBytes());
        CommandLineSettings settings = CommandLineSettings.getSettings(new String[]{});

        Injector injector = Guice.createInjector(new LicenseToolModule(settings));
        HttpClientService httpClientService = injector.getInstance(HttpClientService.class);
        JsoupProvider jsoupProvider = injector.getInstance(JsoupProvider.class);

        GoSumFileReader goSumFileReader = new GoSumFileReader(in, httpClientService, jsoupProvider);
        Collection<IContentId> contentIds = goSumFileReader.getContentIds();

        assertEquals(1, contentIds.size());
        assertTrue(contentIds.stream().allMatch(id -> id.getName().equals("kustomize")));
        assertTrue(contentIds.stream().allMatch(id -> id.getSource().equals("github")));
        assertTrue(contentIds.stream().allMatch(id -> id.getNamespace().equals("kubernetes-sigs")));
        assertTrue(contentIds.stream().allMatch(id -> id.getVersion().equals("aa1dd9ddc28df4800d4262715c5a925fc5e0b1cf")));
    }

    // Advanced case: Some git projects could be repository with golang sub-modules.
    // For example such project structure:
    //
    // github.com/kubernetes-sigs
    //     |
    //   kustomize
    //     |
    //     |--- api
    //     |     |--- go.mod
    //     |     |--- go.sum
    //     |
    //     |--- kyaml
    //     |     |--- go.mod
    //     |     |--- go.sum
    //     |
    //     |--- cmd
    //     |     |--- config
    //     |           |--- go.mod
    //     |           |--- go.sum
    //     |
    //    ...
    //     |--- go.mod
    //     |--- go.sum
    @Test
    public void shouldHandleDependencyFromMultiPackageGithubRepo() throws IOException {
        // We have dependency - golang sub-module "api"
        String goSumFileWithSubModuleReference = "sigs.k8s.io/kustomize/api v0.8.8/go.mod h1:He1zoK0nk43Pc6NlV085xDXDXTNprtcyKZVm3swsdNY=";
        InputStream in = new ByteArrayInputStream(goSumFileWithSubModuleReference.getBytes());

        String githubContent = "[{\"name\":\"api/v0.8.8\",\"zipball_url\":\"https://api.github.com/repos/sigs.k8s.io/kustomize/zipball/refs/tags/api/v0.8.8\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/api/v0.8.8\",\"commit\":{\"sha\":\"aa1dd9ddc28df4800d4262715c5a925fc5e0b1cf\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/aa1dd9ddc28df4800d4262715c5a925fc5e0b1cf\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuNC4x\"},{\"name\":\"v0.4.0\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.4.0\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.4.0\",\"commit\":{\"sha\":\"642b87ad9aaf6804f7f9a3f475baf9dd362df027\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/642b87ad9aaf6804f7f9a3f475baf9dd362df027\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuNC4w\"},{\"name\":\"v0.3.1\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.3.1\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.3.1\",\"commit\":{\"sha\":\"3012a1dbe2e4bd1391d42b32f0577cb7bbc7f005\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/3012a1dbe2e4bd1391d42b32f0577cb7bbc7f005\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuMy4x\"},{\"name\":\"v0.3.0\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.3.0\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.3.0\",\"commit\":{\"sha\":\"b26d9c308763d68093482582cea63d69be07a0f0\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/b26d9c308763d68093482582cea63d69be07a0f0\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuMy4w\"},{\"name\":\"v0.2.0\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.2.0\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.2.0\",\"commit\":{\"sha\":\"bbd5bb678321a0d6e58f1099321dfa73391c1b6f\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/bbd5bb678321a0d6e58f1099321dfa73391c1b6f\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuMi4w\"},{\"name\":\"v0.1.0\",\"zipball_url\":\"https://api.github.com/repos/BurntSushi/toml/zipball/refs/tags/v0.1.0\",\"tarball_url\":\"https://api.github.com/repos/BurntSushi/toml/tarball/refs/tags/v0.1.0\",\"commit\":{\"sha\":\"2ceedfee35ad3848e49308ab0c9a4f640cfb5fb2\",\"url\":\"https://api.github.com/repos/BurntSushi/toml/commits/2ceedfee35ad3848e49308ab0c9a4f640cfb5fb2\"},\"node_id\":\"MDM6UmVmODQyNTYyMjpyZWZzL3RhZ3MvdjAuMS4w\"}]";

        doAnswer(ans -> {
            Consumer<InputStream> callback = (Consumer<InputStream>) ans.getArguments()[3];
            InputStream stream = new ByteArrayInputStream(githubContent.getBytes(StandardCharsets.UTF_8.name()));
            callback.accept(stream);
            return null;
        }).when(httpClientService).get(anyString(), anyString(), any(Map.class), any(Consumer.class));

        Elements elements = new Elements();
        Attributes attributes = new Attributes();
        attributes.add("content", "sigs.k8s.io/kustomize git https://github.com/kubernetes-sigs/kustomize.git");
        Element element = new Element(Tag.valueOf("meta"), "", attributes);
        elements.add(element);
        when(jsoupProvider.getDocument(anyString())).thenReturn(document);
        when(document.select("meta[name='go-import']")).thenReturn(elements);

        GoSumFileReader goSumFileReader = new GoSumFileReader(in, httpClientService, jsoupProvider);
        Collection<IContentId> contentIds = goSumFileReader.getContentIds();

        assertEquals(1, contentIds.size());
        assertTrue(contentIds.stream().allMatch(id -> id.getName().equals("kustomize")));
        assertTrue(contentIds.stream().allMatch(id -> id.getSource().equals("github")));
        assertTrue(contentIds.stream().allMatch(id -> id.getNamespace().equals("kubernetes-sigs")));
        assertTrue(contentIds.stream().allMatch(id -> id.getVersion().equals("aa1dd9ddc28df4800d4262715c5a925fc5e0b1cf")));
    }
}
