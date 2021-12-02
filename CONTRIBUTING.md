////
 * Copyright (C) 2020 Eclipse Foundation and others. 
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-FileType: DOCUMENTATION
 * SPDX-FileCopyrightText: 2020 Eclipse Foundation
 * SPDX-License-Identifier: EPL-2.0
////

# Contributing to Eclipse Dash, Tools for Committers

Thanks for your interest in this project.

## Project description

Eclipse Dash is a place where the community itself collaborates on tools for
community awareness and collaboration in support of our ultimate objective of
committer quality and cooperation.

* https://projects.eclipse.org/projects/technology.dash

## Developer resources

Information regarding source code management, builds, coding standards, and
more.

* https://projects.eclipse.org/projects/technology.dash/developer

## Eclipse Contributor Agreement

Before your contribution can be accepted by the project team contributors must
electronically sign the Eclipse Contributor Agreement (ECA).

* http://www.eclipse.org/legal/ECA.php

Commits that are provided by non-committers must have a Signed-off-by field in
the footer indicating that the author is aware of the terms by which the
contribution has been provided to the project. The non-committer must
additionally have an Eclipse Foundation account and must have a signed Eclipse
Contributor Agreement (ECA) on file.

For more information, please see the Eclipse Committer Handbook:
https://www.eclipse.org/projects/handbook/#resources-commit

## Build

The project uses standard Maven to build. 

The project uses Java 11 language features. **Java 11 or greater is required.**

From the root:

```
$ mvn clean install
```

The build generates:

- a shaded JAR, `./core/target/org.eclipse.dash.licenses-<version>.jar` that contains 
everything that is required to run from the command line; and
- a Maven plugin, `org.eclipse.dash:license-tool-plugin:license-check`.


## Contact

Contact the project developers via the project's "dev" list.

* https://dev.eclipse.org/mailman/listinfo/dash-dev
