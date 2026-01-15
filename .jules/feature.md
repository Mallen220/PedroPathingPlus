2024-05-23 - Command Framework Implementation
Learning: The library can operate independently of `solvers-lib` by implementing a local Command-Based framework. This removes external dependencies and allows for tighter integration with PedroPathing features in the future.
Action: Continue expanding the local command framework (e.g., adding Parallel/Sequential groups) to fully replace `solvers-lib`.

2024-05-23 - Testing Environment Constraint
Learning: The development environment lacks a configured Android SDK, preventing `gradlew test` from running unit tests that depend on the Android plugin, even for pure Java code.
Action: Run unit tests manually using `javac` and `java` with the system-provided JUnit jars, or ensure tests are strictly pure Java and do not rely on Android classes if possible.
