# AGENTS.md

## EXTERNAL DEPENDENCIES - READ ONLY

The directory `/external/PedroPathing/` contains a copy of the source code for the `PedroPathing` library (v2.0.0). This copy exists solely for the purpose of symbol resolution, understanding method signatures, and logic reference for AI agents.

### Strict Rules

1.  **Immutable Reference Snapshot**: The contents of `/external/PedroPathing/` are a static snapshot.
2.  **Strictly Read-Only**: You must **never modify, reformat, refactor, rename, delete, or reorganize** any files within `/external/PedroPathing/`. Do not fix bugs or add comments in this directory.
3.  **No Behavioral Coupling**:
    *   Do not import these files in `settings.gradle.kts` or `build.gradle`.
    *   Do not create runtime dependencies or execution paths that rely on the presence of this directory.
    *   The plugin must function correctly even if this directory is absent (e.g., in a production build).
4.  **Ground Truth**: Use this code as the authoritative source for the behavior of the external library. If there is a conflict between your assumptions and this code, defer to this code.

FAILURE TO FOLLOW THESE RULES IS UNACCEPTABLE.
