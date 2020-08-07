# Contributing
## Found a Bug?
If you find a bug in the source code, you can help us by submitting an issue to our GitHub Repository. Even better, you can submit a Pull Request with a fix.

**Considering making your first Pull Request?** You can learn how from this *free* series [How to Contribute to an Open Source Project on GitHub](https://egghead.io/series/how-to-contribute-to-an-open-source-project-on-github)

## Pull Requests
1. Fork the project
2. Implement feature/fix bug & add test cases
3. Ensure test cases & static analysis runs successfully - run `./gradlew check`
4. Submit a pull request to `master` branch

Please include unit tests where necessary to cover any functionality that is introduced.

## Coding Guidelines
* All features or bug fixes **must be tested** by one or more unit tests/specs
* All public API methods **must be documented** in the KDoc/JavaDoc and potentially in the user guide.
* All Kotlin code must follow [Kotlin's Coding Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html).
* All Java code must follow [Google's Java Code style](https://google.github.io/styleguide/javaguide.html), the only excption being that annotations on members or classes may be on the same line (no forced line break).

## Commit messages
Each commit message consists of a header, a body and a footer. The header has a special format that includes a type and a subject:

```
<type>: <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

The **header** is mandatory and the **scope** of the header is optional.

Any line of the commit message cannot be longer 100 characters! This allows the message to be easier
to read on GitHub as well as in various git tools.

Footer should contain a [closing reference to an issue](https://help.github.com/articles/closing-issues-via-commit-messages/) if any.

### Revert
If the commit reverts a previous commit, it should begin with `revert: `, followed by the header of the reverted commit. In the body it should say: `This reverts commit <hash>.`, where the hash is the SHA of the commit being reverted.

### Type
Must be one of the following:

* **build**: Changes that affect the build system or external dependencies (example scopes: gradle, fastlane, npm)
* **ci**: Changes to our CI configuration files and scripts (example scopes: Travis, Circle, BrowserStack, SauceLabs)
* **docs**: Documentation only changes
* **feat**: A new feature
* **fix**: A bug fix
* **perf**: A code change that improves performance
* **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
* **test**: Adding missing tests or correcting existing tests
* **refactor**: A code change that neither fixes a bug nor adds a feature (anything other than the above)

### Subject
The subject contains succinct description of the change:

* use the imperative, present tense: "change" not "changed" nor "changes"
* don't capitalize first letter
* no dot (.) at the end

### Body
Just as in the **subject**, use the imperative, present tense: "change" not "changed" nor "changes".
The body should include the motivation for the change and contrast this with previous behavior.

### Footer
The footer should contain any information about **Breaking Changes** and is also the place to
reference GitHub issues that this commit **Closes**.

**Breaking Changes** should start with the word `BREAKING CHANGE:` with a space or two newlines. The rest of the commit message is then used for this.

## Changelog Guidelines
All changes will be documented using a [CHANGELOG](https://github.com/rakutentech/android-miniapp/blob/master/CHANGELOG.md) by writing chronologically for each version with the following format:

```
Release Version (YYYY-MM-DD)
SDK
- <description>
<BLANK LINE>
Sample App
- <description>
```

### Description
A sentence describing the type of change with necessary and relevant information.

```
<bold><change type></bold>: <subject>
```

#### Change type
* **Feature**: Added a new feature or changed an existing feature.
* **Fixed**: When there is a hotfix or bugfix.
* **Upgraded:** Upgraded a dependency or other version.
* **Deprecated**: Still available for use but will no longer be developed or supported. This is an indication it should not be used and will be removed in the next major version release.
* **Obsoleted**: No longer works as expected since a better alternative is available and will maybe discontinued from the next releases.
* **Removed**: When a feature has been expired by the sunsetting deadline.
* Write "No changes" when there is no difference with the previous release version.
