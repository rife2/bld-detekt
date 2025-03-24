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

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
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
    void testBasePath() {
        var foo = new File("foo");
        var bar = new File("bar");

        var op = new DetektOperation().basePath(foo);
        assertThat(op.basePath()).as("as file").isEqualTo(foo.getAbsolutePath());

        op = op.basePath(bar.toPath());
        assertThat(op.basePath()).as("as path").isEqualTo(bar.getAbsolutePath());

        op = op.basePath("foo");
        assertThat(op.basePath()).as("as string").isEqualTo("foo");
    }

    @Test
    void testBaseline() {
        var foo = new File("foo");
        var bar = new File("bar");

        var op = new DetektOperation().baseline(foo);
        assertThat(op.baseline()).as("as file").isEqualTo(foo.getAbsolutePath());

        op = op.baseline(bar.toPath());
        assertThat(op.baseline()).as("as path").isEqualTo(bar.getAbsolutePath());

        op = op.baseline("foo");
        assertThat(op.baseline()).as("as string").isEqualTo("foo");
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    void testCheckAllParameters() throws IOException {
        var args = Files.readAllLines(Paths.get("src", "test", "resources", "detekt-args.txt"));

        assertThat(args).isNotEmpty();

        var op = new DetektOperation()
                .fromProject(new BaseProject())
                .allRules(true)
                .autoCorrect(true)
                .basePath("basePath")
                .basePath(new File("basePath"))
                .baseline("baseline")
                .buildUponDefaultConfig(true)
                .classPath(new File("path1"))
                .classPath("path2", "path3")
                .classPath(List.of(new File("path4"), new File("path5")))
                .config(new File("config1"))
                .config("config2", "config3")
                .config(List.of(new File("config4"), new File("config5")))
                .configResource("configResource")
                .configResource(new File("configResource"))
                .createBaseline(true)
                .debug(true)
                .disableDefaultRuleSets(true)
                .excludes(List.of("excludes1", "excludes2"))
                .excludes("excludes3", "excludes4")
                .generateConfig(true)
                .includes(List.of("includes1", "includes2"))
                .includes("includes3", "includes4", "includes5")
                .input(new File("input1"))
                .input("input2", "input3")
                .input(List.of(new File("input4"), new File("input5")))
                .jdkHome("jdkHome")
                .jvmTarget("jvmTarget")
                .languageVersion("languageVersion")
                .maxIssues(10)
                .parallel(true)
                .plugins(new File("jar1"))
                .plugins("jar2", "jar3")
                .plugins(List.of(new File("jar4"), new File("jar5")))
                .report(new Report(ReportId.HTML, "reports"));

        assertThat(op.excludes()).as("excludes[]").containsExactly(".*/build/.*", ".*/resources/.*",
                "excludes1", "excludes2", "excludes3", "excludes4");

        for (var i = 1; i < 6; i++) {
            assertThat(op.classPath()).as("classPath[" + i + ']').hasSize(5).contains(new File("path" + i));
            assertThat(op.config()).as("config[" + i + ']').hasSize(5).contains(new File("config" + i));
            assertThat(op.includes()).as("includes[" + i + ']').hasSize(5).contains("includes" + i);
            assertThat(op.input()).as("input[" + i + ']').hasSize(5).contains(new File("input" + i));
            assertThat(op.plugins()).as("plugins[" + i + ']').hasSize(5).contains(new File("jar" + i));
        }

        var params = op.executeConstructProcessCommandList();

        try (var softly = new AutoCloseableSoftAssertions()) {
            for (var p : args) {
                var found = false;
                for (var a : params) {
                    if (a.startsWith(p)) {
                        found = true;
                        break;
                    }
                }
                softly.assertThat(found).as(p + " not found.").isTrue();
            }
        }
    }

    @Test
    void testClassPath() {
        var foo = new File("foo");
        var bar = new File("bar");

        var op = new DetektOperation().classPath("foo", "bar");
        assertThat(op.classPath()).as("String...").contains(foo, bar);
        op.classPath().clear();

        op = op.classPath(foo, bar);
        assertThat(op.classPath()).as("File...").contains(foo, bar);
        op.classPath().clear();

        op = op.classPath(foo.toPath(), bar.toPath());
        assertThat(op.classPath()).as("Path...").contains(foo, bar);
        op.classPath().clear();

        op = op.classPathStrings(List.of("foo", "bar"));
        assertThat(op.classPath()).as("List(String...)").contains(foo, bar);
        op.classPath().clear();

        op = op.classPath(List.of(foo, bar));
        assertThat(op.classPath()).as("File...").contains(foo, bar);
        op.classPath().clear();

        op = op.classPathPaths(List.of(foo.toPath(), bar.toPath()));
        assertThat(op.classPath()).as("Path...").contains(foo, bar);
        op.classPath().clear();
    }

    @Test
    void testConfig() {
        var foo = new File("foo");
        var bar = new File("bar");

        var op = new DetektOperation().config("foo", "bar");
        assertThat(op.config()).as("String...").contains(foo, bar);
        op.config().clear();

        op = op.config(foo, bar);
        assertThat(op.config()).as("File...").contains(foo, bar);
        op.config().clear();

        op = op.config(foo.toPath(), bar.toPath());
        assertThat(op.config()).as("Path...").contains(foo, bar);
        op.config().clear();

        op = op.configStrings(List.of("foo", "bar"));
        assertThat(op.config()).as("List(String...)").contains(foo, bar);
        op.config().clear();

        op = op.config(List.of(foo, bar));
        assertThat(op.config()).as("File...").contains(foo, bar);
        op.config().clear();

        op = op.configPaths(List.of(foo.toPath(), bar.toPath()));
        assertThat(op.config()).as("Path...").contains(foo, bar);
        op.config().clear();
    }

    @Test
    void testConfigResource() {
        var foo = new File("foo");
        var bar = new File("bar");

        var op = new DetektOperation().configResource(foo);
        assertThat(op.configResource()).as("as file").isEqualTo(foo.getAbsolutePath());

        op = op.configResource(bar.toPath());
        assertThat(op.configResource()).as("as path").isEqualTo(bar.getAbsolutePath());

        op = new DetektOperation().configResource("foo");
        assertThat(op.configResource()).as("as string").isEqualTo("foo");
    }

    @Test
    void testExampleBaseline() throws IOException, ExitStatusException, InterruptedException {
        var tmpDir = Files.createTempDirectory("bld-detekt-").toFile();

        var baseline = new File(tmpDir, "examples/src/test/resources/detekt-baseline.xml");

        var op = new DetektOperation()
                .fromProject(new BaseProjectBlueprint(new File("examples"), "com.example",
                        "example", "Example"))
                .baseline(baseline)
                .createBaseline(true);
        op.execute();

        deleteOnExit(tmpDir);

        assertThat(baseline).exists();

    }

    @Test
    void testExampleMaxIssues() {
        var op = new DetektOperation()
                .fromProject(new BaseProjectBlueprint(new File("examples"), "com.example",
                        "example", "Example"))
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
                        "example", "Example"))
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
                        "example", "Example"))
                .debug(true);
        assertThatThrownBy(op::execute).isInstanceOf(ExitStatusException.class);
    }

    @Test
    void testExecuteNoProject() {
        var op = new DetektOperation();
        assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
    }

    @Test
    void testInput() {
        var foo = new File("foo");
        var bar = new File("bar");

        var op = new DetektOperation().input("foo", "bar");
        assertThat(op.input()).as("String...").contains(foo, bar);
        op.input().clear();

        op = op.input(foo, bar);
        assertThat(op.input()).as("File...").contains(foo, bar);
        op.input().clear();

        op = op.input(foo.toPath(), bar.toPath());
        assertThat(op.input()).as("Path...").contains(foo, bar);
        op.input().clear();

        op = op.inputStrings(List.of("foo", "bar"));
        assertThat(op.input()).as("List(String...)").contains(foo, bar);
        op.input().clear();

        op = op.input(List.of(foo, bar));
        assertThat(op.input()).as("File...").contains(foo, bar);
        op.input().clear();

        op = op.inputPaths(List.of(foo.toPath(), bar.toPath()));
        assertThat(op.input()).as("Path...").contains(foo, bar);
        op.input().clear();
    }

    @Test
    void testPlugins() {
        var foo = new File("foo");
        var bar = new File("bar");

        var op = new DetektOperation().plugins("foo", "bar");
        assertThat(op.plugins()).as("String...").contains(foo, bar);
        op.plugins().clear();

        op = op.plugins(foo, bar);
        assertThat(op.plugins()).as("File...").contains(foo, bar);
        op.plugins().clear();

        op = op.plugins(foo.toPath(), bar.toPath());
        assertThat(op.plugins()).as("Path...").contains(foo, bar);
        op.plugins().clear();

        op = op.pluginsStrings(List.of("foo", "bar"));
        assertThat(op.plugins()).as("List(String...)").contains(foo, bar);
        op.plugins().clear();

        op = op.plugins(List.of(foo, bar));
        assertThat(op.plugins()).as("File...").contains(foo, bar);
        op.plugins().clear();

        op = op.pluginsPaths(List.of(foo.toPath(), bar.toPath()));
        assertThat(op.plugins()).as("Path...").contains(foo, bar);
        op.plugins().clear();
    }
}
