# Eclipse Dash License Tool

The Eclipse Dash License Tool identifies the licenses of content.

Each individual bit of content is identified by its ClearlyDefined id. This id uniquely defines, for example, a particular version of a JAR file, or a particular version of an NPM module. This tool knows how to read and convert Maven coordinates and NPM ids into ClearlyDefined ids. 

The CLI accepts a flat file with each line containing a content identifier (ClearlyDefined id, Maven coordinates, or NPM identifier), or a node `package-lock.json` file. 

Use the `-summary <file>` option to  generate a file that contains CSV content with one line for each line of input, mapping a package to a license along with whether that content is `approved` for use by an Eclipse project or `restricted`, meaning that the Eclipse IP Team needs to have a look at the Eclipse project's use of that content. This file is suitable for augmenting the IP Log of an Eclipse open source project.

The current implementation uses two sources for license information. The first source is an Eclipse Foundation service that leverages data that the Eclipse Foundation's IP Team has collected over the years. When that source does not have information for a piece of content, [ClearlyDefined](https://clearlydefined.io/)'s service is used. 

The idea was to have some code that can be used to check the licenses of content, but write it in a manner that would make it easy to generate, for example, a Maven plug-in. The main focus, however, has been making this work as a CLI so that it can be used to sort out licenses for Maven, `package-lock.json`, yarn, etc.

## Build

The project uses standard Maven to build. From the root:

```
$ mvn clean package
```

The build generates a shaded JAR, `org.eclipse.dash.licenses-<version>.jar` that contains 
everything that is required to run from the command line.

## Usage

Generate a dependency list from Maven and invoke the tool on the output:

```
$ mvn clean install
$ mvn dependency:list | grep -Poh "\S+:(system|provided|compile)" | sort | uniq \
 | java -jar org.eclipse.dash.licenses-<version>.jar -
```

Note that Maven's `dependency:list` plugin has the ability to output directly to a file 
(rather than pulling this information from `stdout`); if you use this feature, be sure to 
add the `append` switch or all you'll get in the output is the dependencies for the last 
module in your build.

or, if you already have a `package-lock.json` file:

```
$ java -jar org.eclipse.dash.licenses-<version>.jar package-lock.json
```

The output (for now) is either a statement that the licenses for all of the content have
been identified and verified, or a list of those dependencies that require further
scrutiny.

### Example: Maven

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

### Example: Gradle

Find all of the potentially problematic third party libraries from a Gradle build.

```
$ ./gradlew dependencies | grep -Poh "[^:\s]+:[^:]+:[^:\s]+" | grep -v "^org\.eclipse" | sort | uniq \
 | java -jar org.eclipse.dash.licenses-<version>.jar - \
 | grep restricted
```
 
Note that this example pre-filters content that comes from Eclipse projects (`grep -v "^org\.eclipse"`).
 

### Example: Yarn

We provide a tool to generate a dependency list for yarn-based builds.

```
$ (cd <path-to-this-repo>yarn && yarn install)
$ (cd <path-to-project> && node <path-to-this-repo>/yarn/index.js) \
 | java -jar org.eclipse.dash.licenses-<version>.jar -
```

For example:

```
$ node /dash-licenses/yarn/index.js \
| java -jar /dash-licenses/target/org.eclipse.dash.licenses-<version>.jar -
License information could not be automatically verified for the following content:

https://clearlydefined.io/definitions/npm/npmjs/-/typed-rest-client/1.2.0
https://clearlydefined.io/definitions/npm/npmjs/-/azure-devops-node-api/7.2.0
https://clearlydefined.io/definitions/npm/npmjs/-/boolbase/1.0.0
https://clearlydefined.io/definitions/npm/npmjs/-/typescript/3.6.4

Please create contribution questionnaires for this content.
```

### Example: SBT

Find all of the potentially problematic third party libraries from an SBT build.

```
$ ./sbt dependencyTree \
| grep -Poh "(?<=\+\-)[^:]+:[^:]+:[^:\s]+" | sort | uniq \
| java -jar /dash-licenses/org.eclipse.dash.licenses-<version>.jar -
```

###

### Example 5: Help

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


## Help Wanted

Stuff that we need to add:

* Publish Maven plug-in to repository (#45);
* Links to make creating CQs semi-automatic (or even automatic);
* Logging (#9);
* Make the implementation more extensible by adding dependency injection (#8);
* Support for other technologies (e.g., cmake, go, ...) (#10)

For a more complete list, and status, see the open issues.
