# Extract License Information from Content

**This repository will be moved into the Eclipse Dash project.**

The idea was to have some code that can be used to check the licenses of content. 
I've tried to write it in manner that would make it easy to generate, for example, 
a Maven plug-in. My main focus, however, has been making this work as a CLI so that 
it can be used to sort out licenses for Maven, package-lock.json, yarn, etc.

This implementation uses two sources for license information. The first source is an
Eclipse Foundation service that leverages data that the Eclipse Foundation's IP Team
has collected over the years. When that source does not have information for a piece
of content, ClearlyDefined's service are used. 

Content is indentified by ClearlyDefined id. This tool knows how to convert Maven and
pURL coordinates into ClearlyDefined. The CLI accepts a flat file with each line 
representing a piece of content, or a node package-lock.json file. The current 
implementation generates CSV content with one line for each line of input, mapping
a package to a license along with whether that content is `approved` for use by an
Eclipse project or `restricted`, meaning that the IP Team needs to have a look at the
Eclipse project's use of that content.

TBD: add concrete examples.

## Build

The project uses standard Maven to build. From the root:

<pre>&gt; mvn clean package</pre>

The build generates a shaded JAR, `org.eclipse.dash.license-<version>.jar` that contains 
everything that is required to run.

## Usage

Generate a dependency list from Maven and invoke the tool on the output:

<pre>> mvn clean install -DskipTests
> mvn dependency:list | grep -Poh "\S+:(system|provided|compile)" | sort | uniq > maven.deps
> java -jar org.eclipse.dash.license-<version>.jar maven.deps</pre>

Note that Maven's dependency:list plugin has the ability to output directly to a file 
(rather than pulling this information from stdout); if you use this feature, be sure to 
add the `append` switch or all you'll get in the output is the dependencies for the last 
module in your build.

or, if you already have a `package-lock.json` file:

<pre>> java -jar org.eclipse.dash.license-<version>.jar package-lock.json</pre>

or, if you're using `yarn`:

<pre>> yarn list | grep -Poh "(?:([^\/\s]+)\/)?([^\/\s]+)@\D*(\d+(?:\.\d+)*)" > yarn.deps
> java -jar org.eclipse.dash.license-<version>.jar yarn.deps</pre>

The output (for now) is a CSV list.

## Help Wanted

Stuff that we need to add:

* Create a Maven plug-in;
* Add an option with more specific "you need to create a CQ for these";
* Links to make creating CQs semi-automatic (or even automatic);
* Logging;
* Make the implementation more extensible by adding dependency injection;
* Support for other technologies (e.g., cmake, go, ...)
