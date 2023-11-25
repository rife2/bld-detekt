package com.example;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.CompileKotlinOperation;
import rife.bld.extension.DetektOperation;
import rife.bld.extension.dokka.DokkaOperation;
import rife.bld.extension.dokka.LoggingLevel;
import rife.bld.extension.dokka.OutputFormat;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
        downloadSources = true;
        autoDownloadPurge = true;
        repositories = List.of(MAVEN_LOCAL, MAVEN_CENTRAL, RIFE2_RELEASES);

        scope(compile)
                .include(dependency("org.jetbrains.kotlin", "kotlin-stdlib", version(1, 9, 21)));
        scope(test)
                .include(dependency("org.jetbrains.kotlin:kotlin-test-junit5:1.9.21"))
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 10, 1)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 10, 1)));

        // Include the Kotlin source directory when creating or publishing sources Java Archives
        jarSourcesOperation().sourceDirectories(new File(srcMainDirectory(), "kotlin"));
    }

    public static void main(String[] args) {
        var level = Level.ALL;
        var logger = Logger.getLogger("rife.bld.extension");
        var consoleHandler = new ConsoleHandler();

        // Enable detailed logging for the Kotlin extension
        consoleHandler.setLevel(level);
        logger.addHandler(consoleHandler);
        logger.setLevel(level);
        logger.setUseParentHandlers(false);

        new ExampleBuild().start(args);
    }

    @BuildCommand(summary = "Compile the Kotlin project")
    @Override
    public void compile() throws IOException {
        // The source code located in src/main/kotlin and src/test/kotlin will be compiled
        new CompileKotlinOperation()
                .fromProject(this)
                .execute();
    }

    @BuildCommand(summary = "Check source with Detekt")
    public void detekt() throws ExitStatusException, IOException, InterruptedException {
        new DetektOperation()
                .fromProject(this)
                .execute();
    }

    @BuildCommand(value = "detekt-baseline", summary = "Creates a Detekt baseline")
    public void detektBaseline() throws ExitStatusException, IOException, InterruptedException {
        new DetektOperation()
                .fromProject(this)
                .baseline("detekt-baseline.xml")
                .createBaseline(true)
                .execute();
    }
}