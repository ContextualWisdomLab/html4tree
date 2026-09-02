# Third-party notices

This file records the direct dependency licensing boundary for the current `html4tree` Gradle build. It does not replace the license text or attribution requirements of any dependency, and it does not imply that every transitive artifact is covered by the repository's MIT license.

## Runtime / packaged dependencies

The current `build.gradle` declares Kotlin `1.3.72` and Clikt `2.7.1` as compile dependencies. The application JAR is assembled by expanding the compile configuration into the JAR, so these dependencies are redistribution-relevant rather than merely development tools.

- **Kotlin standard library 1.3.72** — Apache License 2.0. Maven Central metadata for `org.jetbrains.kotlin:kotlin-stdlib:1.3.72` declares the Apache License, Version 2.0. Kotlin's transitive runtime artifacts retain their own license and attribution terms.
- **Clikt 2.7.1** (`com.github.ajalt:clikt`) — Apache License 2.0. The upstream Clikt distribution records `Copyright 2018-2020 AJ Alt` and the Apache License 2.0 grant.

Apache-2.0 components are commercially usable under their terms. The repository's MIT license does not relicense them. A distributable fat JAR must preserve every license/NOTICE obligation applicable to the exact resolved runtime graph; source-level notice documentation is not, by itself, proof that a built archive satisfies those obligations.

## Test-only dependencies

- **JUnit 4.13.2** — Eclipse Public License 1.0. It is declared through `testCompile` and is not part of the intended runtime dependency surface.
- **Kotlin test/JUnit support 1.3.72** — test-only Kotlin support; Kotlin artifacts retain their own license terms.

Test-only status must be verified from the exact built artifact rather than inferred from this file if packaging logic changes.

## Current commercial-policy result

Repository search on 2026-09-02 found no GPL/LGPL/AGPL or noncommercial license marker in the checked-in `html4tree` source surface. The direct runtime dependencies identified above are Apache-2.0, and the direct JUnit test dependency is EPL-1.0. No GPL-family component is normalized as acceptable by this notice.

The remaining packaging diligence gap is executable: the current build expands runtime dependency JARs into the application JAR, but the repository does not yet carry an acceptance test proving that the exact distributable preserves all required third-party license/NOTICE material. `docs/product-technical-gap-baseline.md` tracks that gap.

## Source repository license

`html4tree` itself remains under the inherited MIT license in [`LICENSE`](LICENSE), including the original Yamir Encarnacion copyright notice. That grant applies to the repository work under its terms; it does not overwrite the licenses above.
