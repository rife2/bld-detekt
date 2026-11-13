# [bld](https://rife2.com/bld) Extension to Perform Static Code Analysis with [Detekt](https://detekt.dev/) for [Kotlin](https://kotlinlang.org/)


[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![bld](https://img.shields.io/badge/3.0.1-FA9052?label=bld&labelColor=2392FF)](https://rife2.com/bld)
[![Release](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.rife2.com%2Freleases%2Fcom%2Fuwyn%2Frife2%2Fbld-detekt%2Fmaven-metadata.xml&color=blue)](https://repo.rife2.com/#/releases/com/uwyn/rife2/bld-detekt)
[![Snapshot](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.rife2.com%2Fsnapshots%2Fcom%2Fuwyn%2Frife2%2Fbld-detekt%2Fmaven-metadata.xml&label=snapshot)](https://repo.rife2.com/#/snapshots/com/uwyn/rife2/bld-detekt)
[![GitHub CI](https://github.com/rife2/bld-detekt/actions/workflows/bld.yml/badge.svg)](https://github.com/rife2/bld-detekt/actions/workflows/bld.yml)

To install the latest version, add the following to the `lib/bld/bld-wrapper.properties` file:

```properties
bld.extension-detekt=com.uwyn.rife2:bld-detekt
```

For more information, please refer to the [extensions](https://github.com/rife2/bld/wiki/Extensions) documentation.

## Check Source Code with Detekt

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

## Detekt Baseline

To use a Detekt baseline, add the following to your build file:

```java
@BuildCommand(value = "detekt", summary = "Checks source with Detekt")
public void detekt() throws ExitStatusException, IOException, InterruptedException {
    new DetektOperation()
        .fromProject(this)
        .execute();
}
```

To generate the baseline, run:

```console
./bld compile detekt --create-baseline
```

`fromProject(this)` consumes `--create-baseline` and defaults to `detekt-baseline.xml` in the work directory. To use a different file, call `baseline(...)`:

```java
.baseline("lib/detekt-baseline.xml")
```

- [View Examples Project](https://github.com/rife2/bld-detekt/tree/main/examples)

## Setting the Detekt Version

By default, the extension downloads the version defined by the
[`DEFAULT_VERSION`](https://rife2.github.io/bld-detekt/rife/bld/extension/DetektOperation.html#DEFAULT_VERSION)
constant.

You can override this to target a specific release with the `version` method.
Only Detekt `2.x` releases are supported:

```java
new DetektOperation()
    .fromProject(this)
    .version("2.0.0-alpha.0") // String form
// .version(new VersionNumber(2, 0, 0, "alpha.7"))  // VersionNumber form
    .execute();
```

## Template Project

There is also a [Kotlin Template Project](https://github.com/rife2/kotlin-bld-example) with support for [Dokka](https://github.com/rife2/bld-dokka) and the Detekt extensions.

## API Reference

Please check the [DetektOperation documentation](https://rife2.github.io/bld-detekt/rife/bld/extension/DetektOperation.html#method-summary) for all available configuration options.