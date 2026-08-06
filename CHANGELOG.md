# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Fixed

- Generate the inline-style Content Security Policy SHA-256 source expression
  from the exact normalized UTF-8 stylesheet bytes emitted into each generated
  `index.html` file, preventing template whitespace from invalidating the policy.

### Tests

- Add a real generated-file regression test that independently recomputes the
  declared style hash from the emitted `<style>` text.

### Documentation

- Record the CSP byte-identity decision, threat boundary, verification contract,
  and current W3C Working Draft reference in `docs/doctoring`.

### Security
- Enhance crash-consistency of `index.html` generation by using `StandardCopyOption.ATOMIC_MOVE` on supporting filesystems (falling back to standard replacement), protecting against Time-of-Check to Time-of-Use (TOCTOU) file corruption.
