# DALE Android Build Source Provenance

This repository's Android build uses the DALE Termux overlay available in the supplied local build package.

## Source available in the workspace

- Package: `termux-claude-stable-3d-v3.zip` / extracted `termux-claude-stable`
- Available overlay repository revision: `c60c323f3e3fe3de91c1a11044b973588f6c6926`
- Upstream Termux source pinned by the package: `8629e632fcb95da272221be327db653fb24befe9`
- Android compile SDK: 36
- Build-tools: 36.0.0
- NDK: 29.0.14206865
- Java: 21 in CI
- DALE overlay archive payload SHA-256: `1adb1f9da3a00393e5cce63731ebbd86c326145bc0f14eb290fa5f7ddb2bd705`

## Requested local commit

The requested DALE/Termux commit `2686f5ba` (`DALE: add Android CI build pipeline`) was **not present in the accessible workspace package** and was not found in the supplied local files or the accessible GitHub account.

It is therefore not relabeled or reconstructed. The workflow records the actual available source revision above.

## Overlay transport

The compressed overlay is stored as two ordered Base64 parts:

- `dale-android/source/overlay.b64.part00`
- `dale-android/source/overlay.b64.part01`

GitHub Actions reconstructs the archive, verifies the exact SHA-256, verifies gzip/tar integrity, and only then extracts it. This avoids relying on an oversized binary upload.

## Pinned-source compatibility

The available DALE shortcut overlay referenced `@drawable/ic_launcher_foreground`, while the pinned Termux source provides `@drawable/ic_foreground`. CI applies a deterministic one-line resource-name substitution immediately before the overlay is applied. This is an explicit compatibility fix against the pinned source, not a runtime-generated resource.

## Stability controls

The workflow uses:

- Ubuntu 24.04
- Temurin JDK 21
- Android API 36
- Build-tools 36.0.0
- NDK 29.0.14206865
- pinned upstream Termux commit
- pinned DALE overlay SHA-256
- Gradle `--no-daemon`
- Gradle maximum workers = 2
- Gradle parallel execution disabled
- workflow timeout and concurrency controls

The existing Ruby/Sinatra files at repository root are preserved. No signing keys or credentials are stored in the repository.
