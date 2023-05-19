<!--
 * Copyright (C) 2020,2022 Eclipse Foundation and others. 
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

The Eclipse Dash License Tool identifies the licenses of content. It is intended primarily for use by Eclipse committers to vet third party content used by their Eclipse open source project.

Each individual bit of content is identified by its ClearlyDefined id. This id uniquely defines, for example, a particular version of a JAR file, or a particular version of an NPM module. This tool knows how to read and convert Maven coordinates and NPM ids into ClearlyDefined ids. 

The Eclipse Dash License Tool does not identify dependencies (at least not in general). Rather, the value it provides starts after the list of dependencies are identified by build tools. That is, the tool works on the list of dependencies with which is it provided (the Maven plugin is an exception to this; it does discover dependencies). Ultimately, the tool is only as good as the input with which it is provided and it is up to the committer to ensure that the input provided is correct (that is, dependencies that are not automatically discovered by build tools must also be vetted per the Eclipse IP Policy). 

The CLI accepts a flat file with each line containing a content identifier (ClearlyDefined id, Maven coordinates, or NPM identifier); it also supports a small number of file formats including `package-lock.json` or `yarn.lock` files. A Maven plugin that is capable of processing a dependency list extracted from a `pom.xml` file is also provided. 

Use the `-summary <file>` option to  generate a file that contains CSV content with one line for each line of input, mapping a package to a license along with whether that content is `approved` for use by an Eclipse project or `restricted`, meaning that the Eclipse IP Team needs to have a look at the Eclipse project's use of that content. This file is suitable for augmenting the IP Log of an Eclipse open source project.

The current implementation uses two sources for license information. The first source is an Eclipse Foundation service that leverages data that the Eclipse Foundation's IP Team has collected over the years (and continues to collect). When that source does not have information for a piece of content, [ClearlyDefined](https://clearlydefined.io/)'s service is used. 

The idea was to have some code that can be used to check the licenses of content, but write it in a manner that would make it easy to generate, for example, a Maven plug-in. The main focus, however, has been making this work as a CLI so that it can be used to sort out licenses for Maven, `package-lock.json`, `yarn.lock`, etc.

More information about the Eclipse Dash License Tool:

- [Eclipse Dash License Tool Maven Plugin](https://blog.waynebeaton.ca/posts/ip/dash-license-tool-maven-plugin/)
- [Eclipse Dash License Tool and The Maven Reactor](https://blog.waynebeaton.ca/posts/ip/dash-license-tool-maven-reactor)
- [Eclipse Dash License Tool and Package Lock Files](https://blog.waynebeaton.ca/posts/ip/dash-license-tool-package-lock/)

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

When the tool identifies a library that requires further review, the obvious question is: now what?

The tool incorporates a feature that can create an issue against the [IPLab](https://gitlab.eclipse.org/eclipsefdn/emo-team/iplab) repository on the Eclipse Foundation's GitLab infrastructure.

To use this feature, you must have committer status on at least one Eclipse project.

* Get an [authentication token](https://gitlab.eclipse.org/-/profile/personal_access_tokens) (scope: `api`) from `gitlab.eclipse.org`;
* Include the `-review` option;
* Pass the token via the `-token` option; and
* Pass the Eclipse open source project id (e.g., `technology.dash`) via the `-project` option.

Note that the options are slightly different for the [Maven plugin](README.md#maven-plugin-options).

**Do not share your access token.**

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
```

**Do not share your access token.**

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

You can use the [IP Team Review request](README.md#automatic-ip-team-review-requests) feature to automatically set up a review.

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

### Example: Maven Plugin

To call the Dash License Tool plugin via `mvn` CLI:

```
$ mvn org.eclipse.dash:license-tool-plugin:license-check -Ddash.summary=DEPENDENCIES 
[INFO] Scanning for projects...
[INFO] 
[INFO] -------------< org.eclipse.dash:org.eclipse.dash.licenses >-------------
[INFO] Building org.eclipse.dash.licenses 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- license-tool-plugin:1.0.0:license-check (default-cli) @ org.eclipse.dash.licenses ---
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
			<version>1.0.0</version>
			<executions>
				<execution>
					<id>license-check</id>
					<goals>
						<goal>license-check</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
		...
	</plugins>
	...
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
	...
</pluginRepositories>
```

#### Maven Plugin Options

The Maven Plugin has the following options that can passed either via the command-line or as configuration options in your `pom.xml` file:

- `dash.skip` - Skip executing the plugin. Default: `false`.
- `dash.fail` - Force the build to fail when license issues are found. Default: `false`. 
- `dash.iplab.token` - The access token for automatically creating IP Team review requests. **Do not share your access token.**
- `dash.projectId` - The Eclipse open source project id (e.g. `technology.dash`)
- `dash.summary` - The location (where) to generate the summary file.
- `dash.review.summary` - The location (where) to generate the review-summary file.

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

### Eclipse Tycho

The Eclipse Dash License Tool's Maven plugin uses the standard Maven Reactor to determine the list of dependencies that it needs to check.

Eclipse Tycho uses a different mechanism to resolve dependencies that is not always invoked.

Add `-Dtycho.target.eager=true` to force Tycho to resolve the dependencies.

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
	</dependencies>
	...
</project>
```

Maven will skip matching content when it resolves dependencies.

Note that this "problem" is not a _Dash License Tool problem_, but rather is a dependency management problem. Try not to think in terms of making changes to satisfy quirks of this particular tool, but rather of making your project's dependency list as accurate as possible. Other tools, like the GitHub Dependabot and the [OSS Review Toolkit](https://github.com/oss-review-toolkit/ort), use this information as well.

### Example: Maven

Consider using the [Maven plugin](#example-maven-plugin) instead.

You can use the Maven `dependency` plugin to generate a list of dependencies and then feed that list into the tool.

```
$ mvn verify dependency:list -DskipTests -Dmaven.javadoc.skip=true -DappendOutput=true -DoutputFile=maven.deps
$ java -jar org.eclipse.dash.licenses-<version>.jar maven.deps
...
$ _
```

> :warning: If your project is a multi-module Maven project, you should provide an absolute path to `-DoutputFile` to append all dependencies in the same file (see [MDEP-542](https://issues.apache.org/jira/browse/MDEP-542)).

You can use the [IP Team Review request](README.md#automatic-ip-team-review-requests) feature to automatically set up a review.

```
$ mvn verify dependency:list -DskipTests -Dmaven.javadoc.skip=true -DappendOutput=true -DoutputFile=maven.deps
$ java -jar org.eclipse.dash.licenses-<version>.jar maven.deps -review -project <project> -token <token>
...
$ _
```

In the case where the license information for content is not already known, this will create review requests (the current implementation will make five requests maximum). **Do not share your access token.**

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
 
### Example: Yarn

If you've generated a `yarn.lock` file, you can feed it directly to the license tool. 

```
$ java -jar org.eclipse.dash.licenses-<version>.jar yarn.lock
License information could not be automatically verified for the following content:

npm/npmjs/-/babel-polyfill/6.26.0
npm/npmjs/-/binaryextensions/2.3.0
npm/npmjs/-/concurrently/3.6.1

This content is either not correctly mapped by the system, or requires review.

```

### Example .NET

Use the `dotnet` command to get the list of transitive dependencies, filter out all but the actual dependency references, strip out the "system" components, convert the remaining entries into ClearlyDefined format, and then feed the results to the Eclipse Dash License Tool.

**NOTE** the example below is a guess at something that we believe should work in PowerShell. It's based on an example of the output provided to us that was generated by executing `dotnet list package --include-transitive` on what we believe is a Windows host system, but the post processing was all done in Linux. Again, we believe that this should work in PowerShell, but have not tested it there. Your feedback and pull requests are welcome.

```
dotnet list package --include-transitive | grep ">" | grep -Pv "\s(Microsoft|NETStandard|NuGet|System|runtime)" | sed -E -e "s/\s+> ([a-zA-Z\.\-]+).+\s([0-9]+\.[0-9]+\.[0-9]+)\s+/nuget\/nuget\/\-\/\1\/\2/g" | java -jar /gitroot/dash/org.eclipse.dash.licenses/core/target/org.eclipse.dash.licenses-<version>.jar -
```

### Example: Rust/Cargo

The `cargo tree -e normal --prefix none --no-dedupe` command can give us a list of dependencies. If we reformat those dependencies as ClearlyDefined IDs, they can be piped directly into the Dash License Tool. 

For this, we can use `sed`:

```
$ cargo tree -e normal --prefix none --no-dedupe \
| sort -u \
| grep -v '^[[:space:]]*$' \
| grep -v zenoh \
| sed -E 's|([^ ]+) v([0-9]+\.[0-9]+\.[0-9]+).*|crate/cratesio/-/\1/\2|' \
| java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -jar /gitroot/dash/org.eclipse.dash.licenses/core/target/org.eclipse.dash.licenses-<version>.jar -
```

Steps:

1. Use `cargo` to generate a dependency list;
2. Sort the results and remove duplicates;
3. Remove empty lines;
4. Remove references to project code;
5. Map each line to a ClearlyDefined ID; and
6. Invoke the tool.

The above example skips code from the Eclipse Zenoh project. Anything that is not _third-party_ content can be removed in a similar manner.

Note that, in order to better leverage ClearlyDefined data, the "v" should **not** be included in the version number. For example, `serde_json v1.0.85` becomes `crate/cratesio/-/serde_json/1.0.85`.

### Example Python

Use the `requirements.txt` file that's generated by the compiler to check dependencies:

```
cat requirements.txt | grep -v \# \
| sed -E -e 's|([^= ]+)==([^= ]+)|pypi/pypi/-/\1/\2|' -e 's| ||g' \
| sort | uniq \
| java -jar /dash-licenses/org.eclipse.dash.licenses-<version>.jar -
```

Steps:

1. Prune out the comment lines;
2. Map each line to a ClearlyDefined ID;
3. Sort the results and remove duplicates; and
4. Invoke the tool.

Alternatively, use `pipdeptree` to find all of the dependencies in your _virtual environment_, convert them into ClearlyDefined IDs, and pipe the results to the Eclipse Dash License Tool:

```
$ pipdeptree -a -f \
| sed -E -e 's|([^= ]+)==([^= ]+)|pypi/pypi/-/\1/\2|' -e 's| ||g' \
| sort | uniq \
| java -jar /dash-licenses/org.eclipse.dash.licenses-<version>.jar -
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

### Reusable Github workflow for automatic license check and IP Team Review Requests 

**EXPERIMENTAL**

Eclipse projects that use Maven for building can use the following workflow to set up an automatic license vetting check for their project,
that also allows committer to request automatic IP reviews via comments:

```
# This workflow will check for Maven projects if the licenses of all (transitive) dependencies are vetted.
name: License vetting status check
on:
  push:
    branches: 
      - 'master'
  pull_request:
    branches: 
     - 'master'
  issue_comment:
    types: [created]
jobs:
  call-license-check:
    uses: eclipse/dash-licenses/.github/workflows/mavenLicenseCheck.yml@master
    with:
      projectId: <PROJECT-ID>
    secrets:
      gitlabAPIToken: ${{ secrets.<PROJECT-NAME>_GITLAB_API_TOKEN }}
```
Projects that have to be set up in advance can use the `setupScript` parameter to pass a script that is executed before the license-check build is started.
Projects that want their git submodules to be checked out and processed can use the 'submodule' parameter.

On each pull-reqest event (i.e. a new PR is created or a new commit for it is pushed) the license-status of all project dependencies is checked automatically and in case unvetted licenses are found the check fails.
Committers of that project can request a review from the IP team, by simply adding a comment with body `/request-license-review`.
The github-actions bot reacts with a 'rocket' to indicate the request was understood and is processed.
Attempts to request license review by non-committers are rejected with a thumps-down reaction.
After the license-review build has terminated the github-action bot will reply with a comment to show the result of the license review request.
Committers can later re-run this license-check workflow from the Github actions web-interface to check for license-status changes.

#### Requirements
- Maven based build
- Root pom.xml must reside in the repository root
- An [authentication token (scope: api) from gitlab.eclipse.org](README.md#automatic-ip-team-review-requests) has to be stored in the repositories secret store(Settings -> Scrects -> Actions) with name `M2E_GITLAB_API_TOKEN`.

## Advanced Scenarios

Support for some advanced usage may vary between Maven and stand-alone execution.

### Authenticated Proxies

For Maven builds that need to access web resources through a proxy server, the Maven
settings file (`$M2_HOME/settings.xml`) provides for configuration of the host, port,
and authorization credentials of one or more proxy servers. The Dash License plug-in
also can use these proxy settings to route its requests for web resources through the
proxy.

By default, the first active proxy in the settings is used by Maven plug-ins, including
the Dash License plug-in. To select a different active proxy configuration, simply
configure its ID in your project:

```xml
<build>
	<plugins>
		<plugin>
			<groupId>org.eclipse.dash</groupId>
			<artifactId>license-tool-plugin</artifactId>
			<version>1.0.0</version>
			<configuration>
				<proxy>my-proxy</proxy>
			</configuration>
			<executions>
				<execution>
					<id>license-check</id>
					<goals>
						<goal>license-check</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```

This may also be overridden on the command-line if necessary:

```console
$ mvn -Ddash.proxy=my-proxy clean verify
```

A best practice for storage of the proxy user's password in the `$M2_HOME/settings.xml`
file is to encrypt it using the master password stored in the
`$HOME/.settings-security.xml` file:

```xml
<proxies>
	<proxy>
		<id>my-proxy</id>
		<active>true</active>
		<protocol>http</protocol>
		<host>localhost</host>
		<port>3128</port>
		<username>johndoe</username>
		<password>{GgKsMHh3F80Hr8=}</password>
	</proxy>
</proxies>
```

See the [Maven Encryption Guide][mvncrypt]
for details of configuring servers such as proxies with encrypted credentials.

[mvncrypt]: https://maven.apache.org/guides/mini/guide-encryption.html
