# Reconstruction notes

- Source of truth for the initial pass was the version 1 binary release.
- Recovered classes: 29 Java source files under `src/main/java`.
- Recovered resources: AE2/MI hatch models, blockstates, textures, recipes, loot tables, translations, and the original Mixin configuration.
- AE2, Modern Industrialization, and Jade are compile-time integration dependencies.
- The initial JAR metadata did not declare Jade, although the binary contains direct Jade API references. The reconstructed metadata marks Jade optional so the integration can be isolated in a later compatibility pass.
