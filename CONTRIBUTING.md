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

In order to be able to contribute to Eclipse Foundation projects you must
electronically sign the Eclipse Contributor Agreement (ECA).

* https://www.eclipse.org/legal/ECA.php

The ECA provides the Eclipse Foundation with a permanent record that you agree
that each of your contributions will comply with the commitments documented in
the Developer Certificate of Origin (DCO). Having an ECA on file associated with
the email address matching the "Author" field of your contribution's Git commits
fulfills the DCO's requirement that you sign-off on your contributions.

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
