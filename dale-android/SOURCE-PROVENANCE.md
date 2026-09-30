# DALE Android Build Source Provenance

This repository's Android build uses the DALE Termux overlay that is present in the supplied local build package.

## Source available in the workspace

- Package: `termux-claude-stable-3d-v3.zip` / extracted `termux-claude-stable`
- Available overlay repository revision: `c60c323f3e3fe3de91c1a11044b973588f6c6926`
- Upstream Termux source pinned by the package: `8629e632fcb95da272221be327db653fb24befe9`
- Android compile SDK: 36
- Build-tools: 36.0.0
- NDK: 29.0.14206865
- Java: 21 in CI

## Requested local commit

The requested DALE/Termux commit `2686f5ba` (`DALE: add Android CI build pipeline`) was **not present in the accessible workspace package** and was not found in the supplied local files.

It is therefore not relabeled or reconstructed. The workflow builds only from the available, hash-pinned DALE overlay plus the explicitly pinned upstream Termux source.

## Stability controls

The workflow pins the Android SDK/API, build-tools, NDK, JDK major version, upstream Termux commit, package variant, and low-resource Gradle settings. Existing files in the Ruby/Sinatra application at repository root are left untouched.
