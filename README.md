[![CircleCI](https://circleci.com/gh/rakutentech/android-miniapp.svg?style=svg)](https://circleci.com/gh/rakutentech/android-miniapp)
[![codecov](https://codecov.io/gh/rakutentech/android-miniapp/branch/master/graph/badge.svg)](https://codecov.io/gh/rakutentech/android-miniapp)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

# MiniApp SDK for Android

Provides a set of tools and capabilities to show mini app in Android Applications. The SDK offers features like fetching, caching and displaying of mini app. 
For instructions on implementing in an android application, see the [User Guide](miniapp/USERGUIDE.md).

## How to build

This repository uses submodules for some configuration, so they must be initialized first.

Note: sparse-checkout is optional. Required Git v2.25 or up.
```bash
$ git submodule init
$ git submodule update
$ (cd miniapp/src/main/assets/js-miniapp && git sparse-checkout set js-miniapp-bridge/export/android)
$ ./gradlew assemble
```

## How to test the Sample app

We are still working on this, please watch this space for future updates.

## Contributing

We are still working on this, please watch this space for future updates.
