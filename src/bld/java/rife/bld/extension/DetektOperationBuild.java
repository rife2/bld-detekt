/*
 * Copyright 2023-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rife.bld.extension;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.publish.PublishDeveloper;
import rife.bld.publish.PublishLicense;
import rife.bld.publish.PublishScm;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.compile;
import static rife.bld.dependencies.Scope.test;
import static rife.bld.operations.JavadocOptions.DocLinkOption.NO_MISSING;

public class DetektOperationBuild extends Project {
    public DetektOperationBuild() {
        pkg = "rife.bld.extension";
        name = "DetektOperation";
        version = version(0, 9, 10, "SNAPSHOT");

        javaRelease = 17;

        downloadSources = true;
        autoDownloadPurge = true;

        repositories = List.of(MAVEN_LOCAL, MAVEN_CENTRAL, RIFE2_RELEASES, RIFE2_SNAPSHOTS);

        scope(compile)
                .include(dependency("com.uwyn.rife2", "bld", version(2, 2, 1)))
                .include(dependency("io.gitlab.arturbosch.detekt", "detekt-cli", version(1, 23, 8)));
        scope(test)
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 13, 1)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 13, 1)))
                .include(dependency("org.assertj", "assertj-core", version(3, 27, 3)));
        javadocOperation()
                .javadocOptions()
                .author()
                .docLint(NO_MISSING)
                .link("https://rife2.github.io/bld/")
                .link("https://rife2.github.io/rife2/");

        publishOperation()
                .repository(version.isSnapshot() ? repository("rife2-snapshot") : repository("rife2"))
                .repository(repository("github"))
                .info()
                .groupId("com.uwyn.rife2")
                .artifactId("bld-detekt")
                .description("bld Detekt Extension")
                .url("https://github.com/rife2/bld-detekt")
                .developer(new PublishDeveloper()
                        .id("ethauvin")
                        .name("Erik C. Thauvin")
                        .email("erik@thauvin.net")
                        .url("https://erik.thauvin.net/")
                )
                .license(new PublishLicense()
                        .name("The Apache License, Version 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.txt")
                )
                .scm(new PublishScm()
                        .connection("scm:git:https://github.com/rife2/bld-detekt.git")
                        .developerConnection("scm:git:git@github.com:rife2/bld-detekt.git")
                        .url("https://github.com/rife2/bld-detekt")
                )
                .signKey(property("sign.key"))
                .signPassphrase(property("sign.passphrase"));
    }

    public static void main(String[] args) {
        new DetektOperationBuild().start(args);
    }

    @BuildCommand(summary = "Checks source code with PMD")
    public void pmd() throws Exception {
        new PmdOperation()
                .fromProject(this)
                .ruleSets("config/pmd.xml")
                .execute();
    }

    @Override
    public void test() throws Exception {
        var os = System.getProperty("os.name");
        if (os != null && os.toLowerCase(Locale.US).contains("linux")) {
            new ExecOperation()
                    .fromProject(this)
                    .command("scripts/cliargs.sh")
                    .execute();
        }

        var testResultsDir = "build/test-results/test/";
        var op = testOperation().fromProject(this);
        op.testToolOptions().reportsDir(new File(testResultsDir));

        Exception ex = null;
        try {
            op.execute();
        } catch (Exception e) {
            ex = e;
        }

        var xunitViewer = new File("/usr/bin/xunit-viewer");
        if (xunitViewer.exists() && xunitViewer.canExecute()) {
            var reportsDir = "build/reports/tests/test/";

            Files.createDirectories(Path.of(reportsDir));

            new ExecOperation()
                    .fromProject(this)
                    .command(xunitViewer.getPath(), "-r", testResultsDir, "-o", reportsDir + "index.html")
                    .execute();
        }

        if (ex != null) {
            throw ex;
        }
    }


}
