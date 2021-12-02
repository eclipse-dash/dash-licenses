<!--
 * Copyright (C) 2020 Eclipse Foundation and others. 
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-FileType: DOCUMENTATION
 * SPDX-FileCopyrightText: 2020 Eclipse Foundation
 * SPDX-License-Identifier: EPL-2.0
-->
 
[![REUSE status](https://api.reuse.software/badge/github.com/eclipse/dash-licenses)](https://api.reuse.software/info/github.com/eclipse/dash-licenses)

# Eclipse Dash License Tool

The Eclipse Dash License Tool identifies the licenses of content.

Each individual bit of content is identified by its ClearlyDefined id. This id uniquely defines, for example, a particular version of a JAR file, or a particular version of an NPM module. This tool knows how to read and convert Maven coordinates and NPM ids into ClearlyDefined ids. 

The CLI accepts a flat file with each line containing a content identifier (ClearlyDefined id, Maven coordinates, or NPM identifier), or a node `package-lock.json` file. 

Use the `-summary <file>` option to  generate a file that contains CSV content with one line for each line of input, mapping a package to a license along with whether that content is `approved` for use by an Eclipse project or `restricted`, meaning that the Eclipse IP Team needs to have a look at the Eclipse project's use of that content. This file is suitable for augmenting the IP Log of an Eclipse open source project.

The current implementation uses two sources for license information. The first source is an Eclipse Foundation service that leverages data that the Eclipse Foundation's IP Team has collected over the years. When that source does not have information for a piece of content, [ClearlyDefined](https://clearlydefined.io/)'s service is used. 

The idea was to have some code that can be used to check the licenses of content, but write it in a manner that would make it easy to generate, for example, a Maven plug-in. The main focus, however, has been making this work as a CLI so that it can be used to sort out licenses for Maven, `package-lock.json`, `yarn.lock`, etc.

## Get it

Dash License Tool executable jar is published to Eclipse's Maven repo and available at https://repo.eclipse.org/content/repositories/dash-licenses/org/eclipse/dash/org.eclipse.dash.licenses/ . Latest version can be directly downloaded from https://repo.eclipse.org/service/local/artifact/maven/redirect?r=dash-licenses&g=org.eclipse.dash&a=org.eclipse.dash.licenses&v=LATEST

It's also available as a [Maven plugin](#example-maven-plugin).

Alternatively, you can get the source code and build it yourself. See [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## Usage

The project uses Java 11 language features. **Java 11 or greater is required.**

The Dash License Tool can read a flat file, a `package-lock.json` file, or a `yarn.lock` file. A [Maven plugin](README.md#example-maven-plugin) that works directly from the Maven Reactor is also provided.

In a flat file, dependencies can be expressed one-on-a-line using ClearlyDefined ids (e.g., `maven/mavencentral/org.apache.commons/commons-csv/1.8`, Maven GAVs (e.g., `org.apache.commons:commons-csv:1.8`, or and NPM Id (e.g., `npm/npmjs/-/babel-polyfill/6.26.0`).

Basic invocation takes the following form:

```
$ java -jar org.eclipse.dash.licenses-<version>.jar <dependency-list>
```

For example, to read a `package-lock.json`:

```
$ java -jar org.eclipse.dash.licenses-<version>.jar package-lock.json
```

In lieu of an actual file, generated content can be piped to the Dash License Tool via `stdin` (use a dash to indicate `stdin` as the source). For example:

```
$ echo "maven/mavencentral/org.apache.commons/commons-csv/1.8" | java -jar org.eclipse.dash.licenses-<version>.jar -
```

The output is either a statement that the licenses for all of the content have been identified and verified, or a list of those dependencies that require further scrutiny.

### Automatic IP Team Review Requests 

**EXPERIMENTAL**

When the tool identifies a library that requires further review, the obvious question is: now what?

The traditional means of requesting a review is by creating a [contribution questionnaire](https://www.eclipse.org/projects/handbook/#ip-prereq-cq) to request assistance from the IP Team. This still works; Eclipse committers who are familiar with this process can continue to engage in this manner (use the ClearlyDefined ID as the "Name and Version of the Library" when making the request).

The tool incorporates a new experimental feature that leverages some new technology. Instead of creating a CQ via IPZilla, the tool can create an issue against the Eclipse Foundation's GitLab instance (there is discussion [here](https://gitlab.eclipse.org/eclipsefdn/iplab/emo/-/issues/2)). Note that this feature is still under development and processing in the back end may take a day or two. It's still very experimental, so there will be changes. 

To use this feature, you must have committer status on at least one Eclipse project.

* Get an [authentication token](https://gitlab.eclipse.org/-/profile/personal_access_tokens) (scope: `api`) from `gitlab.eclipse.org`;
* Include the `-review` option;
* Pass the token via the `-token` option; and
* Pass the project id via the `-project` option.

Note that the options are slightly different for the [Maven plugin](README.md#maven-plugin-options).

The tool currently limits the number of libraries that it will send for review (while we are experimenting with this feature, we want to avoid deluging our vetting system with requests). **Do not share your access token.**

Example:

```
$ java -jar org.eclipse.dash.licenses-<version>.jar yarn.lock -review -token <token> -project ecd.theia
License information could not be automatically verified for the following content:

npm/npmjs/-/babel-polyfill/6.26.0
npm/npmjs/-/binaryextensions/2.3.0
npm/npmjs/-/concurrently/3.6.1
npm/npmjs/-/cssmin/0.3.2
npm/npmjs/-/date-fns/1.30.1
...

This content is either not correctly mapped by the system, or requires review.

Setting up a review for npm/npmjs/-/uglify-js/1.3.5.
 - Created: https://gitlab.eclipse.org/eclipsefdn/iplab/iplab/-/issues/90
Setting up a review for npm/npmjs/-/parse5/4.0.0.
 - Created: https://gitlab.eclipse.org/eclipsefdn/iplab/iplab/-/issues/91
Setting up a review for npm/npmjs/-/sanitize-html/1.27.5.
 - Created: https://gitlab.eclipse.org/eclipsefdn/iplab/iplab/-/issues/92
Setting up a review for npm/npmjs/-/jsdom/11.12.0.
 - Created: https://gitlab.eclipse.org/eclipsefdn/iplab/iplab/-/issues/93
Setting up a review for npm/npmjs/-/detect-node/2.0.4.
 - Created: https://gitlab.eclipse.org/eclipsefdn/iplab/iplab/-/issues/94

More content needs to be reviewed.
For now, however, this experimental feature only submits the first five.

```

Please do not incorporate this feature into your automated builds at this time. **Do not share your access token.**

### Example: Single Library

The Dash License Tool CLI processes the information that you provide to it either as a file, or via `stdin`. It doesn't have any interest in where the data comes from (all of the examples that generate a dependency list from build scripts generates the dependency list and then feeds it to the tool). You can use command-line tools to generate input and pipe it into the tool.

To determine whether or an individual library needs further investigation, feed it directly to the tool:

```
$ echo "tech.units:indriya:1.3" | java -jar org.eclipse.dash.licenses-<version>.jar -
License information could not be automatically verified for the following content:

maven/mavencentral/tech.units/indriya/1.3

This content is either not correctly mapped by the system, or requires review.
$ _
```

To test multiple libraries simultaneously, you can separate them with a newline (note that you need to include the `-e` option to make `echo` understand the newline:

```
$ echo -e "tech.units:indriya:1.3\norg.glassfish:jakarta.json:2.0.0" | java -jar org.eclipse.dash.licenses-<version>.jar -
...
$ _
```

You can use the experimental [IP Team Review request](README.md#automatic-ip-team-review-requests) feature to automatically set up a review.

```
$ echo "tech.units:indriya:1.3" | java -jar org.eclipse.dash.licenses-<version>.jar - -review -project <project> -token <token>
License information could not be automatically verified for the following content:

maven/mavencentral/tech.units/indriya/1.3

This content is either not correctly mapped by the system, or requires review.

Setting up a review for maven/mavencentral/tech.units/indriya/1.3.
 - Created: https://gitlab.eclipse.org/eclipsefdn/iplab/iplab/-/issues/113

$ _
```

In the case where the license information for the library is not already known, this will create a review request. **Do not share your access token.**

### Example: Maven

You can use the Maven `dependency` plugin to generate a list of dependencies and then feed that list into the tool.

```
$ mvn verify dependency:list -DskipTests -Dmaven.javadoc.skip=true -DappendOutput=true -DoutputFile=maven.deps
$ java -jar org.eclipse.dash.licenses-<version>.jar maven.deps
...
$ _
```

> :warning: If your project is a multi-module Maven project, you should provide an absolute path to `-DoutputFile` to append all dependencies in the same file (see [MDEP-542](https://issues.apache.org/jira/browse/MDEP-542)).

You can use the experimental [IP Team Review request](README.md#automatic-ip-team-review-requests) feature to automatically set up a review.

```
$ mvn verify dependency:list -DskipTests -Dmaven.javadoc.skip=true -DappendOutput=true -DoutputFile=maven.deps
$ java -jar org.eclipse.dash.licenses-<version>.jar maven.deps -review -project <project> -token <token>
...
$ _
```

In the case where the license information for content is not already known, this will create review requests (the current implementation will make five requests maximum). **Do not share your access token.**

### Example: Maven Plugin

To call the Dash License Tool plugin via `mvn` CLI:

```
$ mvn org.eclipse.dash:license-tool-plugin:license-check -Ddash.summary=DEPENDENCIES 
[INFO] Scanning for projects...
[INFO] 
[INFO] -------------< org.eclipse.dash:org.eclipse.dash.licenses >-------------
[INFO] Building org.eclipse.dash.licenses 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- license-tool-plugin:0.0.1-SNAPSHOT:license-check (default-cli) @ org.eclipse.dash.licenses ---
[INFO] Vetted license information was found for all content. No further investigation is required.
[INFO] Summary file was written to: ./dash-licenses/license-tool-core/DEPENDENCIES
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.434 s
[INFO] Finished at: 2020-12-18T11:27:38Z
[INFO] ------------------------------------------------------------------------

$ cat DEPENDENCIES 
maven/mavencentral/org.glassfish/jakarta.json/2.0.0, EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0, approved, emo_ip_team
maven/mavencentral/org.checkerframework/checker-compat-qual/2.5.3, MIT, approved, clearlydefined
maven/mavencentral/commons-cli/commons-cli/1.4, Apache-2.0, approved, CQ13132
maven/mavencentral/org.apache.commons/commons-csv/1.8, Apache-2.0, approved, clearlydefined
maven/mavencentral/com.google.flogger/flogger/0.5.1, Apache-2.0, approved, clearlydefined
```

Note that the plugin automatically generates a summary file in the `target` directory; use the `-Ddash.summary=<location>` switch to override the default location.

#### Add to your Maven Build

Configure the license check plugin to be auto executed (e.g. in the `verify` phase):

```
<build>
  <plugins>
    <plugin>
	 <groupId>org.eclipse.dash</groupId>
	 <artifactId>license-tool-plugin</artifactId>
	 <version>0.0.1-SNAPSHOT</version>
	 <executions>
	   <execution>
	     <id>license-check</id>
		<phase>verify</phase>
		<goals>
		  <goal>license-check</goal>
		</goals>
	   </execution>
	 </executions>
    </plugin>
  </plugins>
</build>
```

Add the `repo.eclipse.org` plugin repository so that the license check plugin is discoverable (it's not available in Maven Central):

```
<pluginRepositories>
  <pluginRepository>
  	<id>dash-licenses-snapshots</id>
  	<url>https://repo.eclipse.org/content/repositories/dash-licenses-snapshots/</url>
  	<snapshots>
  	  <enabled>true</enabled>
  	</snapshots>
  </pluginRepository>
</pluginRepositories>
```

#### Maven Plugin Options

The Maven Plugin has the following "-D" options:

- `dash.skip` - Skip executing the plugin. Default: `false`.
- `dash.fail` - Force the build to fail when license issues are found. Default: `false`. 
- `dash.iplab.token` - The access token for automatically creating IP Team review requests. **Do not share your access token.**
- `dash.projectId` - The project id
- `dash.summary` - The location (where) to generate the summary file.

Note that the Maven plugin always generates the summary file. The default location is `${project.build.directory}/dash/summary`.

To generate a summary of dependencies named `DEPENDENCIES` in the working directory:

```
$ mvn org.eclipse.dash:license-tool-plugin:license-check -Ddash.summary=DEPENDENCIES
```

To automatically create IP Team review requests for identified content:

```
$ mvn org.eclipse.dash:license-tool-plugin:license-check -Ddash.iplab.token=<token> -Ddash.projectId=technology.dash
```

**Do not share your access token.**

### Troubleshooting Maven Dependencies

There are cases where some transitive dependency with problematic licensing that is neither directly nor indirectly required by your content is dragged in by some other transitive dependency.

The Dash License Tool really doesn't know anything about dependencies. It has no means of making any assessment of whether or not certain content actually winds up in the products that your producing. It only knows about the information that it is fed either directly by you or by the underlying dependency resolution system.

In this case, you can provide Maven with a list of exclusions.

```
<project>
  ...
  <dependencies>
    <dependency>
      <groupId>org.linkedin</groupId>
      <artifactId>org.linkedin.zookeeper-impl</artifactId>
      <version>${linkedin-zookeeper-version}</version>
      <exclusions>
        <exclusion>
          <groupId>org.json</groupId>
          <artifactId>json</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
   ...
</project>
```

Maven will skip matching content when it resolves dependencies.

Note that this "problem" is not a _Dash License Tool problem_, but rather is a dependency management problem. Try not to think in terms of making changes to satisfy quirks of this particular tool, but rather of making your project's dependency list as accurate as possible. Other tools, like the GitHub Dependabot and the [OSS Review Toolkit](https://github.com/oss-review-toolkit/ort), use this information as well.

### Example: Gradle

Find all of the potentially problematic third party libraries from a Gradle build.

```
$ ./gradlew dependencies | grep -Poh "(?<=\s)[\w\.-]+:[\w\.-]+:[^:\s]+" | grep -v "^org\.eclipse" | sort | uniq \
 | java -jar org.eclipse.dash.licenses-<version>.jar - \
 | grep restricted
```
 
Note that this example pre-filters content that comes from Eclipse projects (`grep -v "^org\.eclipse"`).

**Note for Mac users:** `grep` command on Mac doesn't support parameter option `-P` hence `grep -Poh "\S+:(system|provided|compile)"` will fail. Use `-E` option instead i.e. `grep -ohE "\S+:(system|provided|compile)"` or install GNU grep on your Mac via the command:

```
$ brew install grep
```

Afterwards `grep` will be accessible via `ggrep` so `ggrep -Poh "\S+:(system|provided|compile)` will do the trick.
 
### Example: Yarn via `yarn.lock`

If you've generated a `yarn.lock` file, you can feed it directly to the license tool. 

```
$ java -jar org.eclipse.dash.licenses-<version>.jar yarn.lock
License information could not be automatically verified for the following content:

npm/npmjs/-/babel-polyfill/6.26.0
npm/npmjs/-/binaryextensions/2.3.0
npm/npmjs/-/concurrently/3.6.1

This content is either not correctly mapped by the system, or requires review.

```

### Example: Yarn via yarn

We provide a tool to generate a dependency list for yarn-based builds.

```
$ (cd <path-to-this-repo>/yarn && yarn install)
$ (cd <path-to-project> && node <path-to-this-repo>/yarn/index.js) \
 | java -jar org.eclipse.dash.licenses-<version>.jar -
```

For example:

```
$ node /dash-licenses/yarn/index.js \
| java -jar /dash-licenses/target/org.eclipse.dash.licenses-<version>.jar -
License information could not be automatically verified for the following content:

npm/npmjs/-/babel-polyfill/6.26.0
npm/npmjs/-/binaryextensions/2.3.0
npm/npmjs/-/concurrently/3.6.1

This content is either not correctly mapped by the system, or requires review.
```

### Example: SBT

Find all of the potentially problematic third party libraries from an SBT build.

```
$ ./sbt dependencyTree \
| grep -Poh "(?<=\+\-)[^:]+:[^:]+:[^:\s]+" | sort | uniq \
| java -jar /dash-licenses/org.eclipse.dash.licenses-<version>.jar -
```

### Example: Help

The CLI tool does provide help.

```
$ java -jar target/org.eclipse.dash.licenses-<version>.jar -help
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
 -summary <file>                   Output a summary to a file
 -lic,--licenses <url>             Approved Licenses List URL

<file> is the path to a file, or "-" to indicate stdin. Multiple files may
be provided
e.g.,
npm list | grep -Poh "\S+@\d+(?:\.\d+){2}" | sort | uniq | LicenseFinder -
```
