# ChangeLog

## [1.1.0] - 2023-12-01

The biggest potential impact with this release should be that when the tool is used on
a `package-lock.json` file, it may detect _fewer_ entries as dependencies. Some additional
work was done to refine how the tool identifies _local_ dependencies, which are generally
dependencies that are in the source repository (that is, local dependencies are most
likely project code).

### Changed

- Refactored out a `core` module that does not shade dependencies #283
- Update all dependencies to latest and greatest
- Now uses `jakarta.inject` annotations #264
- More resilient handling of Bad Gateway (502) errors
- More resilient handling of rate limiting on when connecting to IPLab #260
- Ignores all "local" dependencies when parsing a `package-lock.json` file #287 

### Added

- Created a new module for creating a `shaded` artifact (the shaded artifact uses the same naming convention as before, so this should not break anybody) #283
- Added a new `repo` property includes the URL of the Git repository as part of review requests #224