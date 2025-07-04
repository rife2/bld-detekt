package com.example;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.CompileKotlinOperation;
import rife.bld.extension.DetektOperation;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.compile;
import static rife.bld.dependencies.Scope.test;

public class ExampleBuild extends Project {
    public ExampleBuild() {
        pkg = "com.example";
        name = "Example";
        mainClass = "com.example.Example";
        version = version(0, 1, 0);

        javaRelease = 17;

        autoDownloadPurge = true;
        downloadSources = true;

        repositories = List.of(MAVEN_LOCAL, MAVEN_CENTRAL, RIFE2_RELEASES);

        final var kotlin = version(2, 2, 0);
        scope(compile)
                .include(dependency("org.jetbrains.kotlin", "kotlin-stdlib", kotlin));
        scope(test)
                .include(dependency("org.jetbrains.kotlin", "kotlin-test-junit5", kotlin))
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 13, 1)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 13, 1)))
                .include(dependency("org.junit.platform", "junit-platform-launcher", version(1, 13, 1)));

        // Include the Kotlin source directory when creating or publishing sources Java Archives
        jarSourcesOperation().sourceDirectories(new File(srcMainDirectory(), "kotlin"));
    }

    public static void main(String[] args) {
        // Enable detailed logging
        var level = Level.ALL;
        var logger = Logger.getLogger("rife.bld.extension");
        var consoleHandler = new ConsoleHandler();

        consoleHandler.setLevel(level);
        logger.addHandler(consoleHandler);
        logger.setLevel(level);
        logger.setUseParentHandlers(false);

        new ExampleBuild().start(args);
    }

    @BuildCommand(summary = "Compiles the Kotlin project")
    @Override
    public void compile() throws Exception {
        new CompileKotlinOperation()
                .fromProject(this)
                .execute();
    }

    @BuildCommand(summary = "Checks source with Detekt")
    public void detekt() throws ExitStatusException, IOException, InterruptedException {
        // The source code located in the project will be checked
        new DetektOperation()
                .fromProject(this)
                .execute();
    }

    @BuildCommand(value = "detekt-baseline", summary = "Creates the Detekt baseline")
    public void detektBaseline() throws ExitStatusException, IOException, InterruptedException {
        // The detekt-baseline.xml file will be created in the project's root
        new DetektOperation()
                .fromProject(this)
                .baseline("detekt-baseline.xml")
                .createBaseline(true)
                .execute();
    }

    @BuildCommand(value = "detekt-main", summary = "Checks main source with Detekt")
    public void detektMain() throws ExitStatusException, IOException, InterruptedException {
        // The source code located in src/main/kotlin will be checked
        new DetektOperation()
                .fromProject(this)
                .input("src/main/kotlin")
                .execute();
    }

    @BuildCommand(value = "detekt-test", summary = "Checks test source with Detekt")
    public void detektTest() throws ExitStatusException, IOException, InterruptedException {
        // The source code located in src/test/kotlin will be checked
        new DetektOperation()
                .fromProject(this)
                .input("src/test/kotlin")
                .execute();
    }

    @BuildCommand(value = "test-ci", summary = "Run detekt with a test baseline")
    public void testCi() throws ExitStatusException, IOException, InterruptedException {
        // Run detekt with the test baseline (for CI testing)
        new DetektOperation()
                .fromProject(this)
                .baseline("src/test/resources/detekt-baseline.xml")
                .execute();
    }
}
