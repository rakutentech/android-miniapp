[![Download](https://api.bintray.com/packages/ssed-oss-jcenter/ssed-mobile-libs/android-miniapp/images/download.svg)](https://bintray.com/ssed-oss-jcenter/ssed-mobile-libs/android-miniapp/_latestVersion)
[![CircleCI](https://circleci.com/gh/rakutentech/android-miniapp.svg?style=svg)](https://circleci.com/gh/rakutentech/android-miniapp)
[![codecov](https://codecov.io/gh/rakutentech/android-miniapp/branch/master/graph/badge.svg)](https://codecov.io/gh/rakutentech/android-miniapp)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

# MiniApp SDK for Android

Provides a set of tools and capabilities to show mini app in Android Applications. The SDK offers features like fetching, caching and displaying of mini app. 
For instructions on implementing in an android application, see the [User Guide](https://rakutentech.github.io/android-miniapp/).

## How to build

This repository uses submodules for some configuration, so they must be initialized first.

```bash
$ git submodule update --init
$ ./gradlew assemble
```

Next, you must define a few settings used by the Sample App. These can be defined either as environment variables or Gradle properties (in your `~/.gradle/gradle.properties` file).

```
MINIAPP_SERVER_BASE_URL=https://www.example.com/
HOST_PROJECT_ID=test-host-project-id
HOST_APP_SUBSCRIPTION_KEY=test-subs-key
HOST_APP_UA_INFO=MiniAppDemoApp/1.0.0-SNAPSHOT
HOST_APP_VERSION=test-host-app-version
```

Finally, you can build the project via Gradle:

```bash
$ ./gradlew assembleDebug
```

### How to Build the Production Sample App

First you will need to create the keystore file at `testapp/release-keystore.jks`. Next you must define environment variables for the keystore settings.

```
MINIAPP_RELEASE_KEY_ALIAS=my_alias
MINIAPP_RELEASE_KEY_PASSWORD=my_password
MINIAPP_KEYSTORE_PASSWORD=my_keystore_password
```

Next, you must define the prod settings for the Sample App as either environment variables are Gradle Properties.

```
MINIAPP_PROD_SERVER_BASE_URL=https://www.example.com/
HOST_PROJECT_PROD_ID=test-host-project-id
HOST_APP_PROD_SUBSCRIPTION_KEY=test-subs-key
HOST_APP_PROD_UA_INFO=MiniAppDemoApp/1.0.0
HOST_APP_PROD_VERSION=test-host-app-version
```

Finally, you can build the release config for the Sample App.

```bash
$ ./gradlew assembleRelease
```

## How to Test the Sample App

We currently don't provide an API for public use, so you must provide your own API.
Alternatively you can launch your MiniApp on a local sever and then use 'Load by URL' option in the Sample App.

## Writing and generating documentation

Our documentation is hosted on Github Pages using the `gh-pages` branch of this repo. So this means that the docs are hosted as markdown and then Github Pages generates HTML using Jekyll. The documentation has two parts: a userguide and the API docs. The userguide is generated from [USERGUIDE.md](miniapp/USERGUIDE.md) and the API docs are generated using Dokka.

For the most part, you can use standard markdown in the userguide, but please note the following:

- If you wish to use a `<details>` tag for an expandable section, then you must use the following format (note that the closing `</summary>` tag is on a new line):
```xml
<details><summary markdown="span">Title goes here
</summary>

    Content goes here.
</details>
```

### How to generate KDocs SDK documentation locally

You may want to generate the SDK documentation locally so that you can ensure that the generated docs look correct. We use Dokka for this, so you can run the following command, and the generated docs will be output at `miniapp/build/publishableDocs` in the markdown format. 

```
$ ./gradlew generatePublishableDocs
```

The docs are hosted on Github Pages in markdown, and therefore the HTML version is only generated after the docs are published to Github. If you wish to preview the HTML version of the docs, you can do so by copying the markdown files to the `gh-pages` branch and locally building the HTML:

```
$ ./gradlew generatePublishableDocs
$ git checkout gh-pages
$ cp -r miniapp/build/publishableDocs/docs/ ./
$ bundle install
$ bundle exec jekyll serve
```

## Continuous Integration and Deployment

[CircleCI](https://circleci.com/gh/rakutentech/android-miniapp) is used for building and testing the project for every pull request. It is also used for publishing the SDK and the Sample App. 

We use jobs from two CircleCI Orbs (see the [android-buildconfig](https://github.com/rakutentech/android-buildconfig/tree/master/circleci) repo): [android-sdk Orb](https://github.com/rakutentech/android-buildconfig/blob/master/circleci/android-sdk/README.md) and [app-center Orb](https://github.com/rakutentech/android-buildconfig/blob/master/circleci/app-center/README.md). See the Readme for those Orbs for more information on what the jobs do.

### Merge to Master

The following describes the steps CircleCI performs when a branch is merged to master.

1. We trigger CircleCI by merging a branch to master.
2. CI builds the SDK and Sample App, run tests, linting, etc.
3. CI publishes staging build of Sample App to App Center "Testers" group.

### Release

The following describes the steps CircleCI performs to release a new version of the SDK.

1. We trigger CircleCI by pushing a git tag to this repo which is in the format `vX.X.X`.
2. CI builds SDK and Sample App, run tests, linting, etc.
3. CI pauses the workflow for verification from user.
4. After approval, CI publishes the SDK to [Bintray](https://bintray.com/ssed-oss-jcenter/ssed-mobile-libs/android-miniapp).
5. CI publishes generated documentation to [Github Pages site](https://rakutentech.github.io/android-miniapp/).
6. CI publishes production build of Sample App to App Center's "Production" group.

*Note:* It's also possible to publish a snapshot version of the SDK by using a `-` in the version name, such as `v1.0.0-alpha` (and the generated version name will be `1.0.0-SNAPSHOT`). In this case, the snapshot will be published to the [JFrog OSS Snapshot repository](https://oss.jfrog.org/) and the documentation publishing will be skipped.

## Contributing

See our [Contribution Guide](.github/CONTRIBUTING.md).

## Changelog

See the [Changelog](CHANGELOG.md).