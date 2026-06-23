## 2024-03-02 - Kotlin Auto-Generated Property Methods Coverage
**Learning:** In Kotlin, using `var` to declare properties automatically generates hidden getter and setter methods. When using JaCoCo with a strict 100% line or method coverage requirement, these implicit methods can cause coverage failures even if the explicitly written code is fully tested.
**Action:** When enforcing 100% test coverage in Kotlin codebases using JaCoCo, ensure you explicitly test property reassignment and access (even if trivial) to cover the auto-generated getters and setters.
