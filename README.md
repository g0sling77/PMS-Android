# Obscura Mobile (Android ARM64)

Android shell for [Obscura](https://github.com/h4ckf0r0day/obscura).

## Status

**Developer preview only.** This repository currently builds an Android UI shell.
The original Rust/V8 browser engine is **not yet integrated** and the UI does not execute browser operations.
Do not mistake the debug APK for a functional Obscura port.

Target: Xiaomi 14 / Android arm64-v8a.

## Build

GitHub Actions builds a debug APK on pushes to main. Download the APK artifact from the Actions run.

## Next engineering steps

1. Build `deno_core` / `rusty_v8` for `aarch64-linux-android` using the Android NDK; verify snapshot and linker support.
2. Add a Rust `cdylib` exposing a minimal JNI API with explicit error handling.
3. Port Obscura runtime and Android-compatible network, filesystem, font and rendering paths.
4. Connect UI to the JNI API and test on physical Xiaomi 14.
5. Add CDP localhost service with authentication and lifecycle controls.

## Attribution

Obscura upstream is Apache-2.0 licensed. This UI is a separate Android integration scaffold.
