/*
 * Copyright 2023-2024 the original author or authors.
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import rife.bld.BaseProject;
import rife.bld.blueprints.BaseProjectBlueprint;
import rife.bld.extension.detekt.Report;
import rife.bld.extension.detekt.ReportId;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;


@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class DetektOperationTest {
    @BeforeAll
    static void beforeAll() {
        var level = Level.ALL;
        var logger = Logger.getLogger("rife.bld.extension");
        var consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(level);
        logger.addHandler(consoleHandler);
        logger.setLevel(level);
        logger.setUseParentHandlers(false);
    }

    static void deleteOnExit(File folder) {
        folder.deleteOnExit();
        for (var f : Objects.requireNonNull(folder.listFiles())) {
            if (f.isDirectory()) {
                deleteOnExit(f);
            } else {
                f.deleteOnExit();
            }
        }
    }

    @Test
    void testCheckAllParameters() throws IOException {
        var args = Files.readAllLines(Paths.get("src", "test", "resources", "detekt-args.txt"));

        assertThat(args).isNotEmpty();

        var params = new DetektOperation()
                .fromProject(new BaseProject())
                .allRules(true)
                .autoCorrect(true)
                .basePath("basePath")
                .baseline("baseline")
                .buildUponDefaultConfig(true)
                .classPath("classpath")
                .classPath(List.of("path2", "path3"))
                .config("config")
                .config(List.of("config2", "config4"))
                .configResource("configResource")
                .createBaseline(true)
                .debug(true)
                .disableDefaultRuleSets(true)
                .excludes("excludes")
                .generateConfig(true)
                .includes("patterns")
                .input("input")
                .jdkHome("jdkHome")
                .jvmTarget("jvmTarget")
                .languageVersion("languageVersion")
                .maxIssues(10)
                .parallel(true)
                .plugins("jars")
                .plugins(List.of("jars2", "jar3"))
                .report(new DetektReport(DetektReportId.HTML, "reports"))
                .executeConstructProcessCommandList();

        for (var p : args) {
            var found = false;
            for (var a : params) {
                if (a.startsWith(p)) {
                    found = true;
                    break;
                }
            }
            assertThat(found).as(p + " not found.").isTrue();
        }
    }

    @Test
    void testExampleBaseline() throws IOException, ExitStatusException, InterruptedException {
        var tmpDir = Files.createTempDirectory("bld-detekt-").toFile();

        var baseline = new File(tmpDir, "detekt-baseline.xml");

        var op = new DetektOperation()
                .fromProject(new BaseProjectBlueprint(new File("examples"), "com.example",
                        "Example"))
                .baseline(baseline.getAbsolutePath())
                .createBaseline(true);
        op.execute();

        deleteOnExit(tmpDir);

        assertThat(baseline).exists();

    }

    @Test
    void testExampleMaxIssues() {
        var op = new DetektOperation()
                .fromProject(new BaseProjectBlueprint(new File("examples"), "com.example",
                        "Example"))
                .maxIssues(8);
        assertThatNoException().isThrownBy(op::execute);
    }

    @Test
    void testExampleReports() throws IOException {
        var tmpDir = Files.createTempDirectory("bld-detekt-").toFile();

        var html = new File(tmpDir, "report.html");
        var xml = new File(tmpDir, "report.xml");
        var txt = new File(tmpDir, "report.txt");
        var md = new File(tmpDir, "report.md");
        var sarif = new File(tmpDir, "report.sarif");

        var op = new DetektOperation()
                .fromProject(new BaseProjectBlueprint(new File("examples"), "com.example",
                        "Example"))
                .report(new Report(ReportId.HTML, html.getAbsolutePath()))
                .report(new Report(ReportId.XML, xml.getAbsolutePath()))
                .report(new Report(ReportId.TXT, txt.getAbsolutePath()))
                .report(new Report(ReportId.MD, md.getAbsolutePath()))
                .report(new Report(ReportId.SARIF, sarif.getAbsolutePath()));

        assertThatThrownBy(op::execute).isInstanceOf(ExitStatusException.class);

        deleteOnExit(tmpDir);

        List.of(html, xml, txt, md, sarif).forEach(it -> assertThat(it).exists());
    }

    @Test
    void testExamplesExecute() {
        var op = new DetektOperation()
                .fromProject(new BaseProjectBlueprint(new File("examples"), "com.example",
                        "Example"))
                .debug(true);
        assertThatThrownBy(op::execute).isInstanceOf(ExitStatusException.class);
    }
}
