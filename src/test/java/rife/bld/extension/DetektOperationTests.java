/*
 * Copyright 2023-2026 the original author or authors.
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import rife.bld.BaseProject;
import rife.bld.Project;
import rife.bld.blueprints.BaseProjectBlueprint;
import rife.bld.extension.detekt.Report;
import rife.bld.extension.detekt.ReportId;
import rife.bld.extension.testing.LoggingExtension;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(LoggingExtension.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class DetektOperationTests {
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(DetektOperation.class.getName());

    @Test
    void allOverloadedMethods() {
        // Test File, Path, and String overloads
        var baseFile = new File("/tmp/base");
        var baselinePath = Path.of("/tmp/baseline.xml");
        var configResourceString = "my-config.yml";

        var op = new DetektOperation()
                .basePath(baseFile)
                .baseline(baselinePath)
                .configResource(configResourceString);

        assertThat(op.basePath()).isEqualTo(baseFile.getAbsolutePath());
        assertThat(op.baseline()).isEqualTo(baselinePath.toFile().getAbsolutePath());
        assertThat(op.configResource()).isEqualTo(configResourceString);

        // Test collection overloads
        var inputFile = new File("input.kt");
        var pluginPath = Path.of("plugin.jar");
        var configString = "config.yml";
        var classpathString = "cp.jar";

        op.input(inputFile)
                .plugins(pluginPath)
                .config(configString)
                .classPath(classpathString);

        assertThat(op.input()).extracting(File::getPath).contains("input.kt");
        assertThat(op.plugins()).extracting(File::getPath).contains("plugin.jar");
        assertThat(op.config()).extracting(File::getPath).contains("config.yml");
        assertThat(op.classPath()).extracting(File::getPath).contains("cp.jar");
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

        @Test
        void configAsFileArray() {
            var op = new DetektOperation();
            op.config(foo, bar);
            assertThat(op.config()).contains(foo, bar);
        }

        @Test
        void configAsFileList() {
            var op = new DetektOperation();
            op.config(List.of(foo, bar));
            assertThat(op.config()).contains(foo, bar);
        }

        @Test
        void configAsPathArray() {
            var op = new DetektOperation();
            op = op.config(foo.toPath(), bar.toPath());
            assertThat(op.config()).contains(foo, bar);
        }

        @Test
        void configAsPathList() {
            var op = new DetektOperation();
            op.configPaths(List.of(foo.toPath(), bar.toPath()));
            assertThat(op.config()).as("Path...").contains(foo, bar);
        }

        @Test
        void configAsStringArray() {
            var op = new DetektOperation();
            op.config("foo", "bar");
            assertThat(op.config()).contains(foo, bar);
        }

        @Test
        void configAsStringList() {
            var op = new DetektOperation();
            op.configStrings(List.of("foo", "bar"));
            assertThat(op.config()).contains(foo, bar);
        }

        @Test
        void configResourceAsFile() {
            var op = new DetektOperation();
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
            var op = new DetektOperation();
            op.configResource("foo");
            assertThat(op.configResource()).isEqualTo("foo");
        }
    }

    @Nested
    @DisplayName("Example Tests")
    class ExampleTests {
        @TempDir
        private File tmpDir;

        @Test
        void exampleBaseline() throws IOException, ExitStatusException, InterruptedException {
            var baseline = new File(tmpDir, "examples/src/test/resources/detekt-baseline.xml");
            var op = new DetektOperation()
                    .fromProject(new BaseProjectBlueprint(new File("examples"), "com.example",
                            "example", "Example"))
                    .baseline(baseline)
                    .createBaseline(true);

            op.execute();
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
        void exampleReports() {
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

            try (var softly = new AutoCloseableSoftAssertions()) {
                for (var i = 1; i < 6; i++) {
                    softly.assertThat(op.classPath()).as("classPath[%s]", i).hasSize(5)
                            .contains(new File("path" + i));
                    softly.assertThat(op.config()).as("config[%s]", i).hasSize(5)
                            .contains(new File("config" + i));
                    softly.assertThat(op.includes()).as("includes[%s]", i).hasSize(5)
                            .contains("includes" + i);
                    softly.assertThat(op.input()).as("input[%s]", i).hasSize(5)
                            .contains(new File("input" + i));
                    softly.assertThat(op.plugins()).as("plugins[%s]", i).hasSize(5)
                            .contains(new File("jar" + i));
                }

                var params = op.executeConstructProcessCommandList();
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
                var op = new DetektOperation();
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
                var op = new DetektOperation();
                op.basePath("foo");
                assertThat(op.basePath()).isEqualTo("foo");
            }
        }

        @Nested
        @DisplayName("Baseline Tests")
        class BaselineTests {
            @Test
            void baselineAsFile() {
                var op = new DetektOperation();
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
                var op = new DetektOperation();
                op.baseline("foo");
                assertThat(op.baseline()).isEqualTo("foo");
            }
        }

        @Nested
        @DisplayName("ClassPath Tests")
        class ClassPathTests {
            @Test
            void classPathAsFileArray() {
                var op = new DetektOperation();
                op.classPath(foo, bar);
                assertThat(op.classPath()).contains(foo, bar);
            }

            @Test
            void classPathAsFileList() {
                var op = new DetektOperation();
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
                var op = new DetektOperation();
                op.classPathPaths(List.of(foo.toPath(), bar.toPath()));
                assertThat(op.classPath()).contains(foo, bar);
            }

            @Test
            void classPathAsStingArray() {
                var op = new DetektOperation();
                op.classPath("foo", "bar");
                assertThat(op.classPath()).contains(foo, bar);
            }

            @Test
            void classPathAsStringList() {
                var op = new DetektOperation();
                op.classPathStrings(List.of("foo", "bar"));
                assertThat(op.classPath()).contains(foo, bar);
            }
        }

        @Nested
        @DisplayName("Input Tests")
        class InputTests {
            @Test
            void inputAsFileArray() {
                var op = new DetektOperation();
                op.input(foo, bar);
                assertThat(op.input()).contains(foo, bar);
            }

            @Test
            void inputAsFileList() {
                var op = new DetektOperation();
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
                var op = new DetektOperation();
                op.inputPaths(List.of(foo.toPath(), bar.toPath()));
                assertThat(op.input()).contains(foo, bar);
            }

            @Test
            void inputAsStringArray() {
                var op = new DetektOperation();
                op.input("foo", "bar");
                assertThat(op.input()).contains(foo, bar);
            }

            @Test
            void inputAsStringList() {
                var op = new DetektOperation();
                op.inputStrings(List.of("foo", "bar"));
                assertThat(op.input()).contains(foo, bar);
            }
        }

        @Nested
        @DisplayName("Plugins Tests")
        class PluginsTests {
            @Test
            void pluginsAsFileArray() {
                var op = new DetektOperation();
                op.plugins(foo, bar);
                assertThat(op.plugins()).contains(foo, bar);
            }

            @Test
            void pluginsAsFileList() {
                var op = new DetektOperation();
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
                var op = new DetektOperation();
                op.pluginsPaths(List.of(foo.toPath(), bar.toPath()));
                assertThat(op.plugins()).contains(foo, bar);
            }

            @Test
            void pluginsAsStringArray() {
                var op = new DetektOperation();
                op.plugins("foo", "bar");
                assertThat(op.plugins()).contains(foo, bar);
            }

            @Test
            void pluginsAsStringList() {
                var op = new DetektOperation();
                op.pluginsStrings(List.of("foo", "bar"));
                assertThat(op.plugins()).contains(foo, bar);
            }
        }
    }

    @Nested
    @DisplayName("Process Command List Tests")
    class ProcessCommandListTests {
        @Test
        void processCommandListWithBooleanFlags() {
            var op = new DetektOperation()
                    .fromProject(new Project())
                    .allRules(true)
                    .autoCorrect(true)
                    .buildUponDefaultConfig(true)
                    .createBaseline(true)
                    .debug(true)
                    .disableDefaultRuleSets(true)
                    .generateConfig(true)
                    .parallel(true);

            var commandList = op.executeConstructProcessCommandList();

            assertThat(commandList).contains(
                    "--all-rules",
                    "--auto-correct",
                    "--build-upon-default-config",
                    "--create-baseline",
                    "--debug",
                    "--disable-default-rulesets",
                    "--generate-config",
                    "--parallel"
            );
        }

        @Test
        void processCommandListWithCollectionFlags() {
            var input1 = new File("/src/main/Input1.kt");
            var input2 = new File("/src/main/Input2.kt");
            var plugin1 = new File("/plugins/plugin1.jar");
            var plugin2 = new File("/plugins/plugin2.jar");
            var config1 = new File("detekt.yml");
            var config2 = new File("detekt-override.yml");
            var cp1 = new File("lib/dep1.jar");
            var cp2 = new File("lib/dep2.jar");

            var op = new DetektOperation()
                    .fromProject(new Project())
                    .input(input1, input2)
                    .plugins(plugin1, plugin2)
                    .config(config1, config2)
                    .classPath(cp1, cp2)
                    .includes(".*Include.*", ".*Keep.*")
                    .excludes(".*Exclude.*"); // Note: fromProject adds defaults, so we test adding more

            var commandList = op.executeConstructProcessCommandList();

            assertThat(commandList).contains("--input", input1.getAbsolutePath() + "," + input2.getAbsolutePath());
            assertThat(commandList).contains("--plugins", plugin1.getAbsolutePath() + "," + plugin2.getAbsolutePath());
            assertThat(commandList).contains("-config", config1.getAbsolutePath() + ";" + config2.getAbsolutePath());
            assertThat(commandList).contains("--classpath", cp1.getAbsolutePath() + File.pathSeparator + cp2.getAbsolutePath());
            assertThat(commandList).contains("--includes", ".*Include.*,.*Keep.*");
            assertThat(commandList).contains("--excludes", ".*/build/.*,.*/resources/.*,.*Exclude.*");
        }

        @Test
        void processCommandListWithPathAndStringFlags() {
            var op = new DetektOperation()
                    .fromProject(new Project())
                    .basePath("/tmp/base")
                    .baseline("/tmp/baseline.xml")
                    .configResource("my-config.yml")
                    .jdkHome("/opt/jdk")
                    .jvmTarget("17")
                    .languageVersion("1.9")
                    .maxIssues(10);

            var commandList = op.executeConstructProcessCommandList();

            assertThat(commandList).contains(
                    "--base-path", "/tmp/base",
                    "--baseline", "/tmp/baseline.xml",
                    "--config-resource", "my-config.yml",
                    "--jdk-home", "/opt/jdk",
                    "--jvm-target", "17",
                    "--language-version", "1.9",
                    "--max-issues", "10"
            );
        }

        @Test
        void processCommandListWithReports() {
            var op = new DetektOperation()
                    .fromProject(new Project())
                    .report(new Report(ReportId.XML, "/reports/detekt.xml"),
                            new Report(ReportId.HTML, "/reports/detekt.html"));

            var commandList = op.executeConstructProcessCommandList();

            assertThat(commandList).contains(
                    "--report", "xml:/reports/detekt.xml",
                    "--report", "html:/reports/detekt.html"
            );
        }

        @Test
        void processCommandListWithZeroMaxIssues() {
            var op = new DetektOperation()
                    .fromProject(new Project())
                    .maxIssues(0);
            var commandList = op.executeConstructProcessCommandList();
            assertThat(commandList).doesNotContain("--max-issues");
        }
    }
}
