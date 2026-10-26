# 0.2.0 — Minecraft 26.1.2 / NeoForge / Java 25

Completes the existing OpenCode migration to `ru.mehtm.emicompendium`, including
build metadata and the JAR audit. Moves professions into the professions module
while preserving the native EMI entrypoint and beacon/conduit recipe identities.

Effects register a custom EMI stack serializer and registry adapter. Copies and
deserialization now preserve stack quantity. Adds EMIffect-compatible
`emiffect/extra_stacks` resource loading with eight current-version source files,
including conduit power and ominous bottles; invalid resources are logged.

Packages full original third-party MIT notices, retaining upstream attribution.
The Git remote and original project display URL are preserved. Adds repeatable
artifact auditing, an isolated dedicated-server smoke harness and a client QA
checklist. Client R/U, rendering, Shift, resource reload and localization checks
remain unverified; see QA-0.2.0.md before treating this as a client-tested release.

Build inputs include six prebuilt addon JARs at the paths in build.gradle and the
local EMI Maven artifact. Building this repository alone requires those inputs.
