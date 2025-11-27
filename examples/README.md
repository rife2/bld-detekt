## Compile the example

```console
./bld compile
```

## Check code with Detekt

```console
./bld detekt

./bld detekt-main
./bld detekt-test
```

## Generate Detekt baseline

```console
./bld detekt-baseline
```

The `detekt-baseline.xml` file will be created in the project's root directory.

## Explore

- [View Build File](https://github.com/rife2/bld-detekt/blob/master/examples/src/bld/java/com/example/ExampleBuild.java)
- [View Wrapper Properties](https://github.com/rife2/bld-detekt/blob/master/examples/lib/bld/bld-wrapper.properties)