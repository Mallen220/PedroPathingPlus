2024-05-23 - Command Framework Implementation
Learning: The library can operate independently of `solvers-lib` by implementing a local Command-Based framework. This removes external dependencies and allows for tighter integration with PedroPathing features in the future.
Action: Continue expanding the local command framework (e.g., adding Parallel/Sequential groups) to fully replace `solvers-lib`.

2024-05-23 - Testing Environment Constraint
Learning: The development environment lacks a configured Android SDK, preventing `gradlew test` from running unit tests that depend on the Android plugin, even for pure Java code.
Action: Run unit tests manually using `javac` and `java` with the system-provided JUnit jars, or ensure tests are strictly pure Java and do not rely on Android classes if possible.

2024-05-23 - Foreign Command Compatibility
Learning: Using `Object` keys for requirements and reflective adapters allows supporting commands from any library without hard dependencies.
Action: Use this pattern when interoperability with unknown or multiple external libraries is required.

2024-05-23 - Dependency Removal
Learning: Removing a core dependency like `solvers-lib` requires careful search-and-replace of imports and verifying that local replacements (like `Command` and `InstantCommand`) are fully compatible with existing usage (like `NamedCommands`).
Action: Always grep for the package name of the removed dependency to catch all usages before finalizing.

2024-05-23 - Build Configuration for External Deps
Learning: Dependencies expected to be in custom repos might sometimes be missing or only available via JitPack with different coordinates.
Action: When a dependency fails to resolve, check alternative repositories (like JitPack) and verify coordinates (GitHub group/repo vs maven group).

2024-05-23 - API Compatibility
Learning: When replacing an external library with a local implementation, ensure the local API (e.g., `Command.schedule()`) matches the methods expected by consuming classes (`ProgressTracker`) to avoid compilation errors.
Action: Check call sites of replaced classes to ensure all used methods are implemented in the new version.
