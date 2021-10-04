package org.eclipse.dash.licenses.cli;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.cli.html_parser.JsoupProvider;
import org.eclipse.dash.licenses.http.HttpClientService;
import org.eclipse.dash.licenses.util.JsonUtils;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GoSumFileReader implements IDependencyListReader {

  private final BufferedReader reader;
  private final HttpClientService clientService;
  private final JsoupProvider jsoupProvider;
  private final String githubToken;
  private final Map<String, String> commonHeaders = new HashMap<>();

  private final int requestPageSize = 100;

  public GoSumFileReader(InputStream input, HttpClientService clientService, JsoupProvider jsoupProvider) {
      InputStreamReader inStreamReader = new InputStreamReader(input);
      this.reader = new BufferedReader(inStreamReader);
      this.clientService = clientService;
      this.jsoupProvider = jsoupProvider;

       // todo notify user, we need to have token to make a lot of GET request to the github api
      githubToken = System.getenv("GITHUB_TOKEN");
      commonHeaders.put("Authorization", "bearer " + githubToken);
  }

  @Override
  public Collection<IContentId> getContentIds() {
    Collection<String> deps = reader.lines().collect(Collectors.toSet());
    Collection<GoLangPackage> modulesOrPackages = deps.stream().map(this::parsePackage)
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(
        GoLangPackage::getPackageName,
        (goLangPackage) -> goLangPackage,
        // Get line with the the latest used package version.
        // It will be actual package version which golang uses for compilation.
        (packageName1, packageName2) -> packageName2
      )).values();

      Collection<GoLangPackage> packagesOrModules = modulesOrPackages.stream()
        .map(this::setUpSourceInfo)
        .collect(Collectors.toList());

    return packagesOrModules.stream()
      .map(this::convertGoPackageOrModuleToContentId)
      .collect(Collectors.toList());
  }

  private IContentId convertGoPackageOrModuleToContentId(GoLangPackage packageOrModule) {
    String packageIdentifier = packageOrModule.getSourceLocation();

    if (packageIdentifier.endsWith(".git")) {
      int offSet = packageIdentifier.lastIndexOf(".git");
      packageIdentifier = packageIdentifier.substring(0, offSet);
    }
    if (packageIdentifier.matches(".*v[0-9]")) {
      int offSet = packageIdentifier.lastIndexOf("v");
      packageIdentifier = packageIdentifier.substring(0, offSet);
    }

    packageIdentifier = packageIdentifier.replace("https://", "");

    // boolean isValid = true; // Todo isSupported? We need to mark dependencies somehow about clearly defined doesn't support them...
    // if (!packageIdentifier.startsWith("github.com")) {
    //   isValid = false;
    // }

    String[] identifierSegments = packageIdentifier.split("/");
    String namespace = identifierSegments[1];
    String name = identifierSegments[2];

    if (packageOrModule.getRevision().matches("v[0-9]+.[0-9]+.[0-9]+")) {
      String tag = packageOrModule.subPackage != null && !packageOrModule.subPackage.matches("v[0-9]+") ?
              packageOrModule.subPackage + "/" + packageOrModule.revision :
              packageOrModule.revision;
      findTagSHA(namespace, name, tag, packageOrModule::setFullSHA, 0);
    } else {
      String shortSHA = packageOrModule.getRevision().split("-")[2];
      getFullSHA(namespace, name, shortSHA, packageOrModule::setFullSHA);
    }

    return ContentId.getContentId("git", "github", namespace, name, packageOrModule.getFullSHA());
  }

  private void getFullSHA(String org, String repoName, String shortSHA, Consumer<String> consumer) {
    this.clientService.get(String.format("https://api.github.com/repos/%s/%s/commits/%s", org, repoName, shortSHA), "application/json", commonHeaders,
      (inputStream) -> {
        JsonObject commit = JsonUtils.readJson(inputStream);
        consumer.accept(commit.get("sha").toString().replace("\"", ""));
    }); // handle error
  }

  private void findTagSHA(String org, String repoName, String tag, Consumer<String> consumer, final int page) {
    this.clientService.get(String.format("https://api.github.com/repos/%s/%s/tags?per_page=%d&page=%d", org, repoName, requestPageSize, page), "application/json", commonHeaders,
      (inputStream) -> {
        JsonArray tagArray = JsonUtils.readJson(inputStream);
        if (tagArray.isEmpty()) {
          return;
        }
        for (JsonValue jsonTag: tagArray) {
          JsonObject jsoTag = jsonTag.asJsonObject();
          String tagName = jsoTag.get("name").toString().replace("\"", "");
          if (tag.equals(tagName)) {
            consumer.accept(jsoTag.get("commit").asJsonObject().get("sha").toString().replace("\"", ""));
            return;
          }
        }
        // search tag on the next page
        findTagSHA(org, repoName, tag, consumer, page + 1);
      }); // Todo handle error with try/catch. Error can be 404, 503 or something like that.
  }

  private GoLangPackage setUpSourceInfo(GoLangPackage packageOrModule) {
    try {
      // We can apply http support, but it looks dangerously to use such dependencies...
      String goModuleInfoUrl = "https://" + packageOrModule.getPackageName() + "?go-get=1";
      Document doc = this.jsoupProvider.getDocument(goModuleInfoUrl);
      retrieveSourceInfoFromMetaTag(packageOrModule, doc, "go-import");
      if (!packageOrModule.getSourceLocation().startsWith("https://github.com")) {
        retrieveSourceInfoFromMetaTag(packageOrModule, doc, "go-source");
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      // handle error...
    }
    return packageOrModule;
  }

  private GoLangPackage retrieveSourceInfoFromMetaTag(GoLangPackage packageOrModule, Document doc, String metaTagName) {
    Elements newsHeadlines = doc.select("meta[name='" + metaTagName + "']");
    for (Element headline : newsHeadlines) {
      Optional<Attribute> attribute = headline.attributes().asList().stream().filter(attr -> attr.getKey().equals("content")).findFirst();
      if (attribute.isPresent()) {
        String content = attribute.get().getValue()
          .replace("\n", "")
          .replace("\r", "");

        String[] segments = content.split("\\s+");
        String moduleOrPackageName = segments[0];
        String sourceURL = segments[2];

        if (packageOrModule.packageName.startsWith(moduleOrPackageName) || packageOrModule.packageName.startsWith("github.com")) {
          packageOrModule.setSourceLocation(sourceURL);

          String packageRoot = moduleOrPackageName.replaceFirst("(http://)|(https://)", "");
          if (!packageOrModule.packageName.equals(packageRoot)) {
            // todo rename to subPackageORModule
            String subPackage = packageOrModule.packageName.replaceFirst(moduleOrPackageName + "/", "");
            packageOrModule.setSubPackage(subPackage);
          }
        }
      }
      System.out.println(headline);
    }
    return packageOrModule;
  }

  private GoLangPackage parsePackage(String line) {
    Pattern pattern = Pattern.compile("(?<packageName>.*) (?<revision>.*) (?<hash>h1:.*)");
    Matcher matcher = pattern.matcher(line);
    if (matcher.find()) {
      String packageName = matcher.group("packageName");
      String revision = matcher.group("revision").replace("/go.mod", "");
      return new GoLangPackage(packageName, revision);
    }
    return null;
  }

  static class GoLangPackage {
    private final String packageName;
    private final String revision;
    private String sourceLocation;
    private String subPackage;
    private String fullSHA; // rename to fullCommitSHA...

    public GoLangPackage(String packageName, String revision) {
      this.packageName = packageName;
      this.revision = revision;
    }

    public String getPackageName() {
      return packageName;
    }

    public String getRevision() {
      return revision;
    }

    public String getSourceLocation() {
      return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
      this.sourceLocation = sourceLocation;
    }

    public String getSubPackage() {
      return subPackage;
    }

    public void setSubPackage(String subPackage) {
      this.subPackage = subPackage;
    }

    public String getFullSHA() {
      return fullSHA;
    }

    public void setFullSHA(String fullSHA) {
      this.fullSHA = fullSHA;
    }
  }
}
