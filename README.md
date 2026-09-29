# Applied Industrialization

NeoForge 1.21.1 addon for Applied Energistics 2 and Modern Industrialization.

Applied Industrialization adds ME-aware pattern input and output hatches for
Modern Industrialization multiblocks, including advanced isolated pattern
input hatches, cross-thread parallel processing, long-count ME output storage,
and optional Productive Bees/ExtendedAE/MIParallelHatch integrations.

## Build

The project uses Java 21 and the NeoForge ModDev Gradle plugin.

```text
gradlew.bat build
```

Required runtime dependencies are Applied Energistics 2 and Modern Industrialization.
Jade, ExtendedAE, and Productive Bees are optional integrations.

The source tree and resources are reconstructed from the compatible
Applied-Industrialization 1.2.7 binary release. Registry IDs are preserved
where they remain applicable; removed legacy hatches are intentionally not
reintroduced by this source snapshot.
