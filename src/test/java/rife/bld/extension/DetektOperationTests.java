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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class DetektOperationTests {
    private static final AtomicBoolean FIRST_TIME = new AtomicBoolean(true);

    @BeforeAll
    static void beforeAll() {
        if (FIRST_TIME.getAndSet(false)) {
            var level = Level.ALL;
            var logger = Logger.getLogger("rife.bld.extension");
            var consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(level);
            logger.addHandler(consoleHandler);
            logger.setLevel(level);
            logger.setUseParentHandlers(false);
        }
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
    void executeNoProject() {
        var op = new DetektOperation();
        assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
    }

    @Nested
    @DisplayName("Config Tests")
    class ConfigTests {
        private final File bar = new File("bar");
        private final File foo = new File("foo");
        private final DetektOperation op = new DetektOperation();

        @Test
        void configAsFileArray() {
            op.config().clear();
            op.config(foo, bar);
            assertThat(op.config()).contains(foo, bar);
        }

        @Test
        void configAsFileList() {
            op.config().clear();
            op.config(List.of(foo, bar));
            assertThat(op.config()).contains(foo, bar);
        }

        @Test
        void configAsPathArray() {
            var op = new DetektOperation();
            op.config().clear();
            op = op.config(foo.toPath(), bar.toPath());
            assertThat(op.config()).contains(foo, bar);
        }

        @Test
        void configAsPathList() {
            op.config().clear();
            op.configPaths(List.of(foo.toPath(), bar.toPath()));
            assertThat(op.config()).as("Path...").contains(foo, bar);
        }

        @Test
        void configAsStringArray() {
            op.config().clear();
            op.config("foo", "bar");
            assertThat(op.config()).contains(foo, bar);
        }

        @Test
        void configAsStringList() {
            op.config().clear();
            op.configStrings(List.of("foo", "bar"));
            assertThat(op.config()).contains(foo, bar);
        }

        @Test
        void configResourceAsFile() {
            op.configResource(foo);
            assertThat(op.configResource()).isEqualTo(foo.getAbsolutePath());
        }

        @Test
        void configResourceAsPath() {
            var op = new DetektOperation();
            op = op.configResource(bar.toPath());
            assertThat(op.configResource()).isEqualTo(bar.getAbsolutePath());
        }

        @Test
        void configResourceAsString() {
            op.configResource("foo");
            assertThat(op.configResource()).isEqualTo("foo");
        }
    }

    @Nested
    @DisplayName("Example Tests")
    class ExampleTests {
        @Test
        void exampleBaseline() throws IOException, ExitStatusException, InterruptedException {
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
        void exampleMaxIssues() {
            var op = new DetektOperation()
                    .fromProject(new BaseProjectBlueprint(new File("examples"), "com.example",
                            "example", "Example"))
                    .maxIssues(8);
            assertThatNoException().isThrownBy(op::execute);
        }

        @Test
        void exampleReports() throws IOException {
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
        void examplesExecute() {
            var op = new DetektOperation()
                    .fromProject(new BaseProjectBlueprint(new File("examples"), "com.example",
                            "example", "Example"))
                    .debug(true);
            assertThatThrownBy(op::execute).isInstanceOf(ExitStatusException.class);
        }
    }

    @Nested
    @DisplayName("Options Tests")
    class OptionsTests {
        private final File bar = new File("bar");
        private final File foo = new File("foo");
        private final DetektOperation op = new DetektOperation();

        @Test
        @EnabledOnOs(OS.LINUX)
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        void checkAllParameters() throws IOException {
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
                assertThat(op.classPath()).as("classPath[%s]", i).hasSize(5).contains(new File("path" + i));
                assertThat(op.config()).as("config[%s]", i).hasSize(5).contains(new File("config" + i));
                assertThat(op.includes()).as("includes[%s]", i).hasSize(5).contains("includes" + i);
                assertThat(op.input()).as("input[%s]", i).hasSize(5).contains(new File("input" + i));
                assertThat(op.plugins()).as("plugins[%s]", i).hasSize(5).contains(new File("jar" + i));
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
                    softly.assertThat(found).as("%s not found.", p).isTrue();
                }
            }
        }

        @Nested
        @DisplayName("Base Path Tests")
        class BasePathTests {
            @Test
            void basePathAsFile() {
                op.basePath(foo);
                assertThat(op.basePath()).isEqualTo(foo.getAbsolutePath());
            }

            @Test
            void basePathAsPath() {
                var op = new DetektOperation();
                op = op.basePath(bar.toPath());
                assertThat(op.basePath()).isEqualTo(bar.getAbsolutePath());
            }

            @Test
            void basePathAsString() {
                op.basePath("foo");
                assertThat(op.basePath()).isEqualTo("foo");
            }
        }

        @Nested
        @DisplayName("Baseline Tests")
        class BaselineTests {
            @Test
            void baselineAsFile() {
                op.baseline(foo);
                assertThat(op.baseline()).isEqualTo(foo.getAbsolutePath());
            }

            @Test
            void baselineAsPath() {
                var op = new DetektOperation();
                op = op.baseline(bar.toPath());
                assertThat(op.baseline()).isEqualTo(bar.getAbsolutePath());
            }

            @Test
            void baselineAsString() {
                op.baseline("foo");
                assertThat(op.baseline()).isEqualTo("foo");
            }
        }

        @Nested
        @DisplayName("ClassPath Tests")
        class ClassPathTests {
            @Test
            void classPathAsFileArray() {
                op.classPath().clear();
                op.classPath(foo, bar);
                assertThat(op.classPath()).contains(foo, bar);
            }

            @Test
            void classPathAsFileList() {
                op.classPath().clear();
                op.classPath(List.of(foo, bar));
                assertThat(op.classPath()).contains(foo, bar);
            }

            @Test
            void classPathAsPathArray() {
                var op = new DetektOperation();
                op = op.classPath(foo.toPath(), bar.toPath());
                assertThat(op.classPath()).contains(foo, bar);
            }

            @Test
            void classPathAsPathList() {
                op.classPath().clear();
                op.classPathPaths(List.of(foo.toPath(), bar.toPath()));
                assertThat(op.classPath()).contains(foo, bar);
            }

            @Test
            void classPathAsStingArray() {
                op.classPath().clear();
                op.classPath("foo", "bar");
                assertThat(op.classPath()).contains(foo, bar);
            }

            @Test
            void classPathAsStringList() {
                op.classPath().clear();
                op.classPathStrings(List.of("foo", "bar"));
                assertThat(op.classPath()).contains(foo, bar);
            }
        }

        @Nested
        @DisplayName("Input Tests")
        class InputTests {
            @Test
            void inputAsFileArray() {
                op.input().clear();
                op.input(foo, bar);
                assertThat(op.input()).contains(foo, bar);
            }

            @Test
            void inputAsFileList() {
                op.input().clear();
                op.input(List.of(foo, bar));
                assertThat(op.input()).contains(foo, bar);
            }

            @Test
            void inputAsPathArray() {
                var op = new DetektOperation();
                op = op.input(foo.toPath(), bar.toPath());
                assertThat(op.input()).contains(foo, bar);
            }

            @Test
            void inputAsPathList() {
                op.input().clear();
                op.inputPaths(List.of(foo.toPath(), bar.toPath()));
                assertThat(op.input()).contains(foo, bar);
                op.input().clear();
            }

            @Test
            void inputAsStringArray() {
                op.input().clear();
                op.input("foo", "bar");
                assertThat(op.input()).contains(foo, bar);
            }

            @Test
            void inputAsStringList() {
                op.input().clear();
                op.inputStrings(List.of("foo", "bar"));
                assertThat(op.input()).contains(foo, bar);
            }
        }

        @Nested
        @DisplayName("Plugins Tests")
        class PluginsTests {
            @Test
            void pluginsAsFileArray() {
                op.plugins().clear();
                op.plugins(foo, bar);
                assertThat(op.plugins()).contains(foo, bar);
            }

            @Test
            void pluginsAsFileList() {
                op.plugins().clear();
                op.plugins(List.of(foo, bar));
                assertThat(op.plugins()).contains(foo, bar);
            }

            @Test
            void pluginsAsPathArray() {
                var op = new DetektOperation();
                op = op.plugins(foo.toPath(), bar.toPath());
                assertThat(op.plugins()).contains(foo, bar);
            }

            @Test
            void pluginsAsPathList() {
                op.plugins().clear();
                op.pluginsPaths(List.of(foo.toPath(), bar.toPath()));
                assertThat(op.plugins()).contains(foo, bar);
                op.plugins().clear();
            }

            @Test
            void pluginsAsStringArray() {
                op.plugins().clear();
                op.plugins("foo", "bar");
                assertThat(op.plugins()).contains(foo, bar);
            }

            @Test
            void pluginsAsStringList() {
                op.plugins().clear();
                op.pluginsStrings(List.of("foo", "bar"));
                assertThat(op.plugins()).contains(foo, bar);
            }
        }
    }
}
