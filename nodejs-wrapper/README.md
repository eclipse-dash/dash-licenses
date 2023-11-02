# dash-licenses-wrapper

This npm package makes it easy to integrate [dash-licenses](https://github.com/eclipse/dash-licenses) to Eclipse Foundation JavaScript/TypeScript projects, including running license checks during CI on GitHub, e.g. on pull requests. That's the best way to catch early, any 3PP components that has incompatible or have unclear licenses. Optionally, `dash-licenses` can run in `automatic review mode`, to automatically create IP Check tickets, on the Eclipse Foundation Gitlab instance, one for each 3PP component that fails the check, for further scrutiny. These tickets can often be approved automatically in minutes.

## Runtime requirements

- called using `bash`
- `node.js` is installed and `node` executable is in the path
- on Linux (tested), probably works on mac and Windows using the Windows Subsystem for Linux (WSL)
- to run `dash-licenses`: JDK or JRE 11 or later is installed and the `java` executable is in the path
- to run `dash-licenses` in [automatic review mode](https://github.com/eclipse/dash-licenses#automatic-ip-team-review-requests):
  - a PAT (Personal Access Token) generated on the Eclipse Foundation Gitlab by an Eclipse project committer
  - local run: the PAT needs to be set in environment variable `DASH_TOKEN`. e.g. `export DASH_TOKEN=<PAT>`
  - GitHub workflow run: the PAT needs to be set as a GitHub Secret, and used in the workflow to define environment variable `DASH_TOKEN`. Note: GitHub [secrets are not available for PRs that originate from a fork](https://docs.github.com/en/actions/security-guides/using-secrets-in-github-actions#using-secrets-in-a-workflow). This means that "automatic review mode" can't be used for PRs from non-committers or PRs from committers that open the PR from a branch in their fork.

## How to install and use

This npm package contains a `dash-licenses-wrapper.js` script that serves as frontend to `dash-licenses`, an example GitHub workflow that uses it and some example configuration files.

To install this package as a "devDependency" in your project, use one of the following from the root, according to the project's npm client:

```bash
# yarn:
# note: if prompted to do so, you may need to add option "--ignore-workspace-root-check"
yarn add dash-licenses-wrapper --dev

# npm:
npm install dash-licenses-wrapper --save-dev
```

Once installed, you can run the license check, from the repo root, with the following command:

```bash
  # using yarn lock file as input (default)
  npx dash-licenses-wrapper

  # using npm lock file as input
  npx dash-licenses-wrapper --inputFile=./package-lock.json
```

To get help about using the wrapper, do:

```bash
  npx dash-licenses-wrapper --help
```

It's suggested to add one or more `scripts` entries in the project's root `package.json`, that call the wrapper, with the wanted arguments. For example:

`npm` project:

```json
"scripts": {
  "license:check": "npm run dash-licenses-wrapper --inputFile=./package-lock.json",
  "license:check:review": "npm run dash-licenses-wrapper --inputFile=./package-lock.json --review --project=ecd.cdt-cloud",
```

`yarn` project:

```json
"scripts": {
  "license:check": "yarn run dash-licenses-wrapper --inputFile=./yarn.lock",
  "license:check:review": "yarn run dash-licenses-wrapper --inputFile=./yarn.lock --review --project=ecd.cdt-cloud",
```

It's possible to use a configuration file instead of adding all wanted options on the CLI. See section below for the details about configuration files.

```bash
npx dash-licenses-wrapper --configFile=./configs/license-check-config.json
```

## configuration file

A configuration file can be used. Values defined therein will override wrapper defaults, and can themselves be overridden by CLI.

`dashLicensesConfig.json`:

```json
{
    "project": "ecd.theia",
    "review": true,
    "inputFile": "./package-lock.json",
    "batch": "50",
    "timeout": "240",
    "exclusionsFile": "configs/dashLicensesExclusions.json",
    "summaryFile": "dash-licenses-summary.txt"
}
```

A configuration file can be used like so:

```bash
 npx dash-licenses-wrapper --configFile=configs/dashLicensesConfig.json
```

## Fine-tuning results: exclusions file

`dash-licenses` is meant to help Eclipse Foundation projects manage the license aspect of their 3PPs, rather than being the absolute "arbitror of truth". As such, a project may want some dependencies, reported as requiring more scrutiny, to be momentarily "ignored".  This means that `dash-licenses` will still process these but the wrapper will not count them as having failed the check. If only excluded 3PPs are reported by `dash-licenses` as requiring further scrutiny, the wrapper will return a success status code.

The `exclusions file` contains one dependency per line, with an optional comment, that can be used to track the justification for excluding the dependency, from failing the license check. This can be useful for example with dependencies that are under review but believed to have a compatible license.

```json
{
  "<3PP as reported by dash-licenses>": "<comment>",
  "<another 3PP>": "<comment>"
}
```

Example scenario: an important Pull Request (PR) adds a 3PP dependency, whose license is believed by the project to be compatible, but for which `dash-licenses` disagrees (e.g. because of a low score). The dependency is submitted the IP team for further analysis but can't be automatically approved, quickly. In the meantime, to avoid delaying merging the important PR or merging and having the "License Check" CI job fail until the dependency is officially approved, it may be added to the `exclusions file`:

Let's say the project's exclusion file is `configs/dashLicensesExclusions.json`

The following entry is added: the first field is the 3PP as reported by `dash-licenses` and the second field is an optional comment, that can be used to track the reason for excluding the dependency from failing the license check. e.g.:

```json
"npm/npmjs/-/<some dependency>/1.2.3": "We believe this dependency is license-compatible. Under review by the IP team to confirm: https://gitlab.eclipse.org/eclipsefdn/emo-team/iplab/-/issues/555555",
```

And then the wrapper can be called with CLI parameter `--exclusions` pointing to the `exclusions` file, like so:

```bash
npx dash-licenses-wrapper --inputFile=./package-lock.json --exclusions=configs/dashLicensesExclusions.json
```

Exclusion file: `<repo_root>/dependency-check-baseline.json`

## GitHub workflow

An example workflow, that runs the license check, is provided in directory `examples` (by default under `node_modules/dash-licenses-wrapper/examples/license-check-workflow.yml`). It can be copied to a GitHub project's directory `<repo root>/.github/workflows` and adapted for the given project.

If the project has added a `scripts` entry in the root `package.json` to run the license check, that may be used instead of `npx dash-licenses-wrapper [...]`. E.g. `yarn license:check [...]`.

It can be very efficient to run `dash-licenses` in `review` mode during CI, to automatically create IP Check tickets, one for each 3PP component that fails the check. To do this, a committer (user with write access to the repo) needs to set a GitHub secret that contains an Eclipse Foundation Gitlab PAT, and use it in the workflow so that environment variable `DASH_TOKEN` is set with it. Note that for security reasons, GitHub only permits the secret to be used when the PR author is a committer. When that's not the case, the wrapper will fall-back to not using `review` mode, and IP tickets should be open manually, as needed.

## Acknowledgements

This work is based on work contributed to the [Eclipse Theia main git repository](https://github.com/eclipse-theia/theia), by Ericsson and others. Mainly script [check_3pp_licenses.js](https://github.com/eclipse-theia/theia/blob/master/scripts/check_3pp_licenses.js).
