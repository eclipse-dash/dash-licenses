# Extract License Information from Content

**This repository will be moved into the Eclipse Dash project.**

This "License Tool" (which clearly needs a more interesting name) identifies the licenses of content.

Each individual bit of content is identified by its ClearlyDefined id. This id uniquely defines, for example, a particular version of a JAR file, or a particular version of an NPM module. This tool knows how to read and convert Maven and pURL coordinates into ClearlyDefined ids. 

The CLI accepts a flat file with each line containing a content identifier (ClearlyDefined id, Maven coordinates, or pUrl), or a node `package-lock.json` file. The current implementation generates CSV content with one line for each line of input, mapping a package to a license along with whether that content is `approved` for use by an Eclipse project or `restricted`, meaning that the Eclipse IP Team needs to have a look at the Eclipse project's use of that content.

The current implementation uses two sources for license information. The first source is an Eclipse Foundation service that leverages data that the Eclipse Foundation's IP Team has collected over the years. When that source does not have information for a piece of content, [ClearlyDefined](https://clearlydefined.io/)'s service is used. 

The idea was to have some code that can be used to check the licenses of content, but write it in a manner that would make it easy to generate, for example, a Maven plug-in. The main focus, however, has been making this work as a CLI so that it can be used to sort out licenses for Maven, `package-lock.json`, yarn, etc.

## Build

The project uses standard Maven to build. From the root:

```
$ mvn clean package
```

The build generates a shaded JAR, `org.eclipse.dash.license-<version>.jar` that contains 
everything that is required to run.

## Usage

Generate a dependency list from Maven and invoke the tool on the output:

```
$ mvn clean install -DskipTests
$ mvn dependency:list | grep -Poh "\S+:(system|provided|compile)" | sort | uniq > maven.deps
$ java -jar org.eclipse.dash.license-<version>.jar maven.deps
```

Note that Maven's dependency:list plugin has the ability to output directly to a file 
(rather than pulling this information from stdout); if you use this feature, be sure to 
add the `append` switch or all you'll get in the output is the dependencies for the last 
module in your build.

or, if you already have a `package-lock.json` file:

```
$ java -jar org.eclipse.dash.license-<version>.jar package-lock.json
```

or, if you're using `yarn`:

```
$ (cd yarn && yarn install)
$ (cd <path-to-project> && node $PWD/yarn/index.js) > yarn.deps
$ java -jar org.eclipse.dash.license-<version>.jar yarn.deps
```

The output (for now) is a CSV list.

### Example 1: Maven

```
$ mvn dependency:list | grep -Poh "\S+:(system|provided|compile)" | sort | uniq > maven.deps
$ cat maven.deps
commons-cli:commons-cli:jar:1.4:compile
commons-codec:commons-codec:jar:1.11:compile
commons-logging:commons-logging:jar:1.2:compile
org.apache.commons:commons-csv:jar:1.6:compile
org.apache.httpcomponents:httpclient:jar:4.5.10:compile
org.apache.httpcomponents:httpcore:jar:4.4.12:compile
org.glassfish:jakarta.json:jar:1.1.6:compile

$ java -jar target/org.eclipse.dash.licenses-0.0.1-SNAPSHOT.jar maven.deps
Vetted license information was found for all content. No further investigation is required.

$ cat DEPENDENCIES
maven/mavencentral/org.glassfish/jakarta.json/1.1.6, EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0, approved, emo_ip_team
maven/mavencentral/commons-codec/commons-codec/1.11, Apache-2.0, approved, CQ15971
maven/mavencentral/commons-cli/commons-cli/1.4, Apache-2.0, approved, CQ13132
maven/mavencentral/org.apache.httpcomponents/httpcore/4.4.12, Apache-2.0, approved, CQ18704
maven/mavencentral/commons-logging/commons-logging/1.2, Apache-2.0, approved, CQ10162
maven/mavencentral/org.apache.httpcomponents/httpclient/4.5.10, Apache-2.0, approved, CQ18703
maven/mavencentral/org.apache.commons/commons-csv/1.6, Apache-2.0, approved, clearlydefined
```

### Example 2: Gradle

Find all of the potentially problematic third party libraries from a Gradle build.

```
$ ./gradlew dependencies | grep -Poh "[^:\s]+:[^:]+:[^:\s]+" | grep -v "^org\.eclipse" | sort | uniq \
 | java -jar /gitroot/dash/org.eclipse.dash.bom/target/org.eclipse.dash.licenses-0.0.1-SNAPSHOT.jar - \
 | grep restricted
```
 
Note that this example pre-filters content that comes from Eclipse projects (`grep -v "^org\.eclipse"`).
 
### Example 3: Help

The CLI tool does provide help.

```
$ java -jar target/org.eclipse.dash.licenses-0.0.1-SNAPSHOT.jar -help df
usage: org.eclipse.dash.licenses.cli.Main [options] <file> ...
Sort out the licenses and approval of dependencies.
 -batch <int>                      Batch size (number of entries sent per
                                   API call)
 -cd,--clearly-defined-api <url>   Clearly Defined API URL
 -confidence <int>                 Confidence threshold expressed as
                                   integer percent (0-100)
 -ef,--foundation-api <url>        Eclipse Foundation license check API
                                   URL.
 -help,--help                      Display help
 -wl,--white-list <url>            License White List URL

<file> is the path to a file, or "-" to indicate stdin. Multiple files may
be provided
e.g.,
npm list | grep -Poh "\S+@\d+(?:\.\d+){2}" | sort | uniq | LicenseFinder -
```

## Help Wanted

Stuff that we need to add:

* Create a Maven plug-in;
* Add an option with more specific "you need to create a CQ for these";
* Links to make creating CQs semi-automatic (or even automatic);
* Logging;
* Make the implementation more extensible by adding dependency injection;
* Support for other technologies (e.g., cmake, go, ...)
