# DALE Shell Contract

The Android APK is a DALE control-plane shell, not a plain terminal launcher.

## Required boot behavior
1. Android launcher entry point opens `DaleLauncherActivity`.
2. TermuxActivity remains the execution terminal and is opened from the Terminal node.
3. CodeChatActivity remains the code/agent workspace and is opened from Code.
4. Workspace opens Android document-tree access.
5. LANIE opens the local OpenAI-compatible endpoint at `127.0.0.1:8080`.
6. RAG, Voice, Vision and Git are visible capability nodes and must not claim a backend is connected unless one is actually present.
7. Horizontal swipe changes the active capability; tapping opens it.
8. The UI is rendered natively with Canvas to keep the shell lightweight on Android.

This contract is enforced by the Android build workflow, which injects and verifies the launcher activity before Gradle compilation.
