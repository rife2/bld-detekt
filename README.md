# [bld](https://rife2.com/bld) Extension to Perform Static Code Analysis with [Detekt](https://detekt.dev/) for [Kotlin](https://kotlinlang.org/)


[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![bld](https://img.shields.io/badge/1.8.0-FA9052?label=bld&labelColor=2392FF)](https://rife2.com/bld)
[![Release](https://flat.badgen.net/maven/v/metadata-url/repo.rife2.com/releases/com/uwyn/rife2/bld-detekt/maven-metadata.xml?color=blue)](https://repo.rife2.com/#/releases/com/uwyn/rife2/bld-detekt)
[![Snapshot](https://flat.badgen.net/maven/v/metadata-url/repo.rife2.com/snapshots/com/uwyn/rife2/bld-detekt/maven-metadata.xml?label=snapshot)](https://repo.rife2.com/#/snapshots/com/uwyn/rife2/bld-detekt)
[![GitHub CI](https://github.com/rife2/bld-detekt/actions/workflows/bld.yml/badge.svg)](https://github.com/rife2/bld-detekt/actions/workflows/bld.yml)

To install, please refer to the [extensions documentation](https://github.com/rife2/bld/wiki/Extensions).

To check all Kotlin source code located in the project, add the following to your build file:
```java
@BuildCommand(summary = "Checks source with Detekt")
public void detekt() throws ExitStatusException, IOException, InterruptedException {
    new DetektOperation()
        .fromProject(this)
        .execute();
}
```

```console
./bld compile detekt
```

- [View Examples Project](https://github.com/rife2/bld-detekt/tree/main/examples)

To generate a Detekt baseline, add the following to your build file:

```java
@BuildCommand(value = "detekt-baseline", summary = "Creates the Detekt baseline")
public void detektBaseline() throws ExitStatusException, IOException, InterruptedException {
    new DetektOperation()
        .fromProject(this)
        .baseline("detekt-baseline.xml")
        .createBaseline(true)
        .execute();
}
```

```console
./bld compile detekt-baseline
```
- [View Examples Project](https://github.com/rife2/bld-detekt/tree/main/examples)

Please check the [DetektOperation documentation](https://rife2.github.io/bld-detekt/rife/bld/extension/DetektOperation.html#method-summary) for all available configuration options.
