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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import rife.bld.BaseProject;
import rife.bld.extension.detekt.Report;
import rife.bld.extension.detekt.ReportId;
import rife.bld.operations.AbstractProcessOperation;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Performs static code analysis with <a href="https://detekt.dev/">Detekt</a>.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class DetektOperation extends AbstractProcessOperation<DetektOperation> {
    private static final List<String> DETEKT_JARS = List.of(
            "annotations-",
            "contester-breakpoint-",
            "detekt-",
            "jcommander-",
            "kotlin-compiler-embeddable-",
            "kotlin-daemon-embeddable-",
            "kotlin-reflect-",
            "kotlin-script-runtime-",
            "kotlin-stdlib-",
            "kotlinx-coroutines-",
            "kotlinx-html-jvm-",
            "kotlinx-serialization-",
            "poko-annotations-jvm-",
            "sarif4k-jvm-",
            "snakeyaml-engine-",
            "trove4j-");
    private static final Logger LOGGER = Logger.getLogger(DetektOperation.class.getName());
    private final List<File> classpath_ = new ArrayList<>();
    private final List<File> config_ = new ArrayList<>();
    private final List<String> excludes_ = new ArrayList<>();
    private final List<String> includes_ = new ArrayList<>();
    private final List<File> input_ = new ArrayList<>();
    private final List<File> plugins_ = new ArrayList<>();
    private final List<Report> report_ = new ArrayList<>();
    private boolean allRules_;
    private boolean autoCorrect_;
    private String basePath_;
    private String baseline_;
    private boolean buildUponDefaultConfig_;
    private String configResource_;
    private boolean createBaseline_;
    private boolean debug_;
    private boolean disableDefaultRuleSets_;
    private boolean generateConfig_;
    private String jdkHome_;
    private String jvmTarget_;
    private String languageVersion_;
    private int maxIssues_;
    private boolean parallel_;
    private BaseProject project_;

    /**
     * Activates all available (even unstable) rules.
     *
     * @param allRules {@code true} or {@code false}
     * @return this operation instance
     */
    public DetektOperation allRules(boolean allRules) {
        allRules_ = allRules;
        return this;
    }

    /**
     * Allow rules to autocorrect code if they support it. The default rule
     * sets do NOT support autocorrecting and won't change any line in the
     * users code base. However, custom rules can be written to support
     * autocorrecting. The additional 'formatting' rule set, added with
     * {@link #plugins(String...) Plugins}, does support it and needs this flag.
     *
     * @param autoCorrect {@code true} or {@code false}
     * @return this operation instance
     */
    public DetektOperation autoCorrect(boolean autoCorrect) {
        autoCorrect_ = autoCorrect;
        return this;
    }

    /**
     * Specifies a directory as the base path. Currently, it impacts all file
     * paths in the formatted reports. File paths in console output and txt
     * report are not affected and remain as absolute paths.
     *
     * @param path the directory path
     * @return this operation instance
     */
    public DetektOperation basePath(String path) {
        basePath_ = path;
        return this;
    }

    /**
     * Specifies a directory as the base path. Currently, it impacts all file
     * paths in the formatted reports. File paths in console output and txt
     * report are not affected and remain as absolute paths.
     *
     * @param path the directory path
     * @return this operation instance
     */
    public DetektOperation basePath(File path) {
        return basePath(path.getAbsolutePath());
    }

    /**
     * Retrieves the base path.
     *
     * @return the directory path
     */
    public String basePath() {
        return basePath_;
    }

    /**
     * Specifies a directory as the base path. Currently, it impacts all file
     * paths in the formatted reports. File paths in console output and txt
     * report are not affected and remain as absolute paths.
     *
     * @param path the directory path
     * @return this operation instance
     */
    public DetektOperation basePath(Path path) {
        return basePath(path.toFile().getAbsolutePath());
    }

    /**
     * If a baseline xml file is passed in, only new code smells not in the
     * baseline are printed in the console.
     *
     * @param baseline the baseline xml file
     * @return this operation instance
     */
    public DetektOperation baseline(String baseline) {
        baseline_ = baseline;
        return this;
    }

    /**
     * If a baseline xml file is passed in, only new code smells not in the
     * baseline are printed in the console.
     *
     * @param baseline the baseline xml file
     * @return this operation instance
     */
    public DetektOperation baseline(File baseline) {
        return baseline(baseline.getAbsolutePath());
    }

    /**
     * If a baseline xml file is passed in, only new code smells not in the
     * baseline are printed in the console.
     *
     * @param baseline the baseline xml file
     * @return this operation instance
     */
    public DetektOperation baseline(Path baseline) {
        return baseline(baseline.toFile().getAbsolutePath());
    }

    /**
     * Retrieves the baseline xml file.
     *
     * @return the baseline xml file
     */
    public String baseline() {
        return baseline_;
    }

    /**
     * Preconfigures detekt with a bunch of rules and some opinionated defaults
     * for you. Allows additional provided configurations to override the
     * defaults.
     *
     * @param buildUponDefaultConfig {@code true} or {@code false}
     * @return this operation instance
     */
    public DetektOperation buildUponDefaultConfig(boolean buildUponDefaultConfig) {
        buildUponDefaultConfig_ = buildUponDefaultConfig;
        return this;
    }

    /**
     * EXPERIMENTAL: Paths where to find user class files and jar dependencies.
     * Used for type resolution.
     *
     * @param paths one or more files
     * @return this operation instance
     * @see #classPath(Collection)
     */
    public DetektOperation classPath(File... paths) {
        return classPath(List.of(paths));
    }

    /**
     * EXPERIMENTAL: Paths where to find user class files and jar dependencies.
     * Used for type resolution.
     *
     * @param paths one or more files
     * @return this operation instance
     * @see #classPathPaths(Collection)
     */
    public DetektOperation classPath(Path... paths) {
        return classPathPaths(List.of(paths));
    }

    /**
     * EXPERIMENTAL: Paths where to find user class files and jar dependencies.
     * Used for type resolution.
     *
     * @param paths one or more files
     * @return this operation instance
     * @see #classPathStrings(Collection)
     */
    public DetektOperation classPath(String... paths) {
        return classPathStrings(List.of(paths));
    }


    /**
     * EXPERIMENTAL: Paths where to find user class files and jar dependencies.
     * Used for type resolution.
     *
     * @param paths the paths
     * @return this operation instance
     * @see #classPath(File...)
     */
    public DetektOperation classPath(Collection<File> paths) {
        classpath_.addAll(paths);
        return this;
    }

    /**
     * Paths where to find user class files and jar dependencies.
     *
     * @return the paths
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<File> classPath() {
        return classpath_;
    }

    /**
     * EXPERIMENTAL: Paths where to find user class files and jar dependencies.
     * Used for type resolution.
     *
     * @param paths the paths
     * @return this operation instance
     * @see #classPath(Path...)
     */
    public DetektOperation classPathPaths(Collection<Path> paths) {
        return classPath(paths.stream().map(Path::toFile).toList());
    }

    /**
     * EXPERIMENTAL: Paths where to find user class files and jar dependencies.
     * Used for type resolution.
     *
     * @param paths the paths
     * @return this operation instance
     * @see #classPath(String...)
     */
    public DetektOperation classPathStrings(Collection<String> paths) {
        return classPath(paths.stream().map(File::new).toList());
    }

    /**
     * Paths to the config files ({@code path/to/config.yml}).
     *
     * @param configs one or more config files
     * @return this operation instance
     * @see #config(Collection)
     */
    public DetektOperation config(File... configs) {
        return config(List.of(configs));
    }

    /**
     * Paths to the config files ({@code path/to/config.yml}).
     *
     * @param configs one or more config files
     * @return this operation instance
     * @see #configPaths(Collection)
     */
    public DetektOperation config(Path... configs) {
        return configPaths(List.of(configs));
    }

    /**
     * Paths to the config files ({@code path/to/config.yml}).
     *
     * @param configs one or more config files
     * @return this operation instance
     * @see #configStrings(Collection)
     */
    public DetektOperation config(String... configs) {
        return configStrings(List.of(configs));
    }

    /**
     * Paths to the config files ({@code path/to/config.yml}).
     *
     * @param configs the config files
     * @return this operation instance
     * @see #config(File...)
     */
    public DetektOperation config(Collection<File> configs) {
        config_.addAll(configs);
        return this;
    }

    /**
     * Paths to config files.
     *
     * @return the config files paths.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<File> config() {
        return config_;
    }

    /**
     * Paths to the config files ({@code path/to/config.yml}).
     *
     * @param configs the config files
     * @return this operation instance
     * @see #config(Path...)
     */
    public DetektOperation configPaths(Collection<Path> configs) {
        return config(configs.stream().map(Path::toFile).toList());
    }

    /**
     * Path to the config resource on detekt's classpath ({@code path/to/config.yml}).
     *
     * @param resource the config resource path
     * @return this operation instance
     */
    public DetektOperation configResource(String resource) {
        configResource_ = resource;
        return this;
    }

    /**
     * Path to the config resource on detekt's classpath ({@code path/to/config.yml}).
     *
     * @param resource the config resource path
     * @return this operation instance
     */
    public DetektOperation configResource(File resource) {
        return configResource(resource.getAbsolutePath());
    }

    /**
     * Path to the config resource on detekt's classpath ({@code path/to/config.yml}).
     *
     * @param resource the config resource path
     * @return this operation instance
     */
    public DetektOperation configResource(Path resource) {
        return configResource(resource.toFile().getAbsolutePath());
    }

    /**
     * Retrieves the path of the config resource.
     *
     * @return the config resource path
     */
    public String configResource() {
        return configResource_;
    }

    /**
     * Paths to the config files ({@code path/to/config.yml}).
     *
     * @param configs the config files
     * @return this operation instance
     * @see #config(String...)
     */
    public DetektOperation configStrings(Collection<String> configs) {
        config_.addAll(configs.stream().map(File::new).toList());
        return this;
    }

    /**
     * Treats current analysis findings as a smell baseline for future detekt
     * runs.
     *
     * @param createBaseline {@code true} or {@code false}
     * @return this operation instance
     */
    public DetektOperation createBaseline(boolean createBaseline) {
        createBaseline_ = createBaseline;
        return this;
    }

    /**
     * Prints extra information about configurations and extensions.
     *
     * @param debug {@code true} or {@code false}
     * @return this operation instance
     */
    public DetektOperation debug(boolean debug) {
        debug_ = debug;
        return this;
    }

    /**
     * Disables default rule sets.
     *
     * @param disable {@code true} or {@code false}
     * @return this operation instance
     */
    public DetektOperation disableDefaultRuleSets(boolean disable) {
        disableDefaultRuleSets_ = disable;
        return this;
    }

    /**
     * Globbing patterns describing paths to exclude from the analysis.
     *
     * @param patterns one or more pattern
     * @return this operation instance
     */
    public DetektOperation excludes(String... patterns) {
        return excludes(List.of(patterns));
    }

    /**
     * Globbing patterns describing paths to exclude from the analysis.
     *
     * @param patterns a collection of patterns
     * @return this operation instance
     */
    public DetektOperation excludes(Collection<String> patterns) {
        excludes_.addAll(patterns);
        return this;
    }

    /**
     * Returns the globbing patterns describing paths to exclude from the analysis.
     *
     * @return the globbing patterns
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<String> excludes() {
        return excludes_;
    }

    /**
     * Performs the operation.
     *
     * @throws InterruptedException when the operation was interrupted
     * @throws IOException          when an exception occurred during the execution of the process
     * @throws ExitStatusException  when the exit status was changed during the operation
     */
    @Override
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public void execute() throws IOException, InterruptedException, ExitStatusException {
        if (project_ == null) {
            if (LOGGER.isLoggable(Level.SEVERE) && !silent()) {
                LOGGER.severe("A project must be specified.");
            }
            throw new ExitStatusException(ExitStatusException.EXIT_FAILURE);
        } else {
            super.execute();
            if (successful_ && LOGGER.isLoggable(Level.INFO) && !silent()) {
                if (createBaseline_) {
                    LOGGER.info("Detekt baseline generated successfully: "
                            + "file://" + new File(baseline_).toURI().getPath());
                } else {
                    LOGGER.info("Detekt operation finished successfully.");
                }
            }
        }
    }

    /**
     * Part of the {@link #execute} operation, constructs the command list
     * to use for building the process.
     */
    @Override
    protected List<String> executeConstructProcessCommandList() {
        final List<String> args = new ArrayList<>(50);
        if (project_ != null) {
            args.add(javaTool());
            args.add("-cp");
            args.add(getDetektJarList(project_.libBldDirectory()));
            args.add("io.gitlab.arturbosch.detekt.cli.Main");

            // all-rules
            if (allRules_) {
                args.add("--all-rules");
            }

            // auto-correct
            if (autoCorrect_) {
                args.add("--auto-correct");
            }

            // base-path
            if (isNotBlank(basePath_)) {
                args.add("--base-path");
                args.add(basePath_);
            }

            // baseline
            if (isNotBlank(baseline_)) {
                args.add("--baseline");
                args.add(baseline_);
            }

            // build-upon-default-config
            if (buildUponDefaultConfig_) {
                args.add("--build-upon-default-config");
            }

            // classpath
            if (!classpath_.isEmpty()) {
                args.add("--classpath");
                args.add(String.join(File.pathSeparator, classpath_.stream().map(File::getAbsolutePath).toList()));
            }

            // config
            if (!config_.isEmpty()) {
                args.add("-config");
                args.add(String.join(";", config_.stream().map(File::getAbsolutePath).toList()));
            }

            // config-resource
            if (isNotBlank(configResource_)) {
                args.add("--config-resource");
                args.add(configResource_);
            }

            // create-baseline
            if (createBaseline_) {
                args.add("--create-baseline");
            }

            // debug
            if (debug_) {
                args.add("--debug");
            }

            // disable-default-rulesets
            if (disableDefaultRuleSets_) {
                args.add("--disable-default-rulesets");
            }

            // excludes
            if (!excludes_.isEmpty()) {
                args.add("--excludes");
                args.add(String.join(",", excludes_));
            }

            // generate-config
            if (generateConfig_) {
                args.add("--generate-config");
            }

            // includes
            if (!includes_.isEmpty()) {
                args.add("--includes");
                args.add(String.join(",", includes_));
            }

            // input
            if (!input_.isEmpty()) {
                args.add("--input");
                args.add(String.join(",", input_.stream().map(File::getAbsolutePath).toList()));
            }

            // jdk-home
            if (isNotBlank(jdkHome_)) {
                args.add("--jdk-home");
                args.add(jdkHome_);
            }

            // jvm-target
            if (isNotBlank(jvmTarget_)) {
                args.add("--jvm-target");
                args.add(jvmTarget_);
            }

            // language-version
            if (isNotBlank(languageVersion_)) {
                args.add("--language-version");
                args.add(languageVersion_);
            }

            // max-issues
            if (maxIssues_ > 0) {
                args.add("--max-issues");
                args.add(String.valueOf(maxIssues_));
            }

            // parallel
            if (parallel_) {
                args.add("--parallel");
            }

            // plugins
            if (!plugins_.isEmpty()) {
                args.add("--plugins");
                args.add(String.join(",", plugins_.stream().map(File::getAbsolutePath).toList()));
            }

            // report
            if (!report_.isEmpty()) {
                report_.forEach(it -> {
                    args.add("--report");
                    args.add(it.id().name().toLowerCase() + ":" + it.path());
                });
            }

            if (LOGGER.isLoggable(Level.FINE) && !silent()) {
                LOGGER.fine(String.join(" ", args.stream().filter(this::isNotBlank).toList()));
            }
        }

        return args;
    }

    /**
     * Configures the operation from a {@link BaseProject}.
     * <p>
     * Sets the following:
     * <ul>
     *     <li>{@link #baseline baseline} to {@code detekt-baseline.xml}, if it exists</li>
     *     <li>{@link #excludes excludes} to exclude {@code build} and {@code resources} directories</li>
     * </ul>
     *
     * @param project the project to configure the operation from
     * @return this operation instance
     */
    @Override
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public DetektOperation fromProject(BaseProject project) {
        project_ = project;
        var baseline = new File(project.workDirectory(), "detekt-baseline.xml");
        if (baseline.exists()) {
            baseline_ = baseline.getAbsolutePath();
        }
        excludes(".*/build/.*", ".*/resources/.*");
        return this;
    }

    /**
     * Export default config. Path can be specified with {@link #config config} option.
     * <p>
     * Default path: {@code default-detekt-config.yml}
     *
     * @param generate {@code true} or {@code false}
     * @return this operation instance
     */
    public DetektOperation generateConfig(boolean generate) {
        generateConfig_ = generate;
        return this;
    }

    // Retrieves the matching JARs files from the given directory.
    private String getDetektJarList(File directory) {
        var jars = new ArrayList<String>();

        if (directory.isDirectory()) {
            var files = directory.listFiles();
            if (files != null) {
                for (var f : files) {
                    if (!f.getName().endsWith("-sources.jar") && !f.getName().endsWith("-javadoc.jar")) {
                        for (var m : DETEKT_JARS) {
                            if (f.getName().startsWith(m)) {
                                jars.add(f.getAbsolutePath());
                                break;
                            }
                        }
                    }
                }
            }
        }
        return String.join(File.pathSeparator, jars);
    }

    /**
     * Globbing patterns describing paths to include in the analysis. Useful in
     * combination with {@link #excludes() excludes} patterns.
     *
     * @param patterns one or more patterns
     * @return this operation instance
     */
    public DetektOperation includes(String... patterns) {
        return includes(List.of(patterns));
    }

    /**
     * Globbing patterns describing paths to include in the analysis. Useful in
     * combination with {@link #excludes() excludes} patterns.
     *
     * @param patterns a collection of patterns
     * @return this operation instance
     */
    public DetektOperation includes(Collection<String> patterns) {
        includes_.addAll(patterns);
        return this;
    }

    /**
     * Returns the globbing patterns describing paths to include in the analysis.
     *
     * @return the globbing patterns
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<String> includes() {
        return includes_;
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths the paths
     * @return this operation instance
     * @see #input(Collection)
     */
    public DetektOperation input(Collection<File> paths) {
        input_.addAll(paths);
        return this;
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths one or more paths
     * @return this operation instance
     * @see #inputStrings(Collection)
     */
    public DetektOperation input(String... paths) {
        return inputStrings(List.of(paths));
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths one or more paths
     * @return this operation instance
     * @see #input(Collection)
     */
    public DetektOperation input(File... paths) {
        return input(List.of(paths));
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths one or more paths
     * @return this operation instance
     * @see #inputPaths(Collection)
     */
    public DetektOperation input(Path... paths) {
        return inputPaths(List.of(paths));
    }

    /**
     * Returns the input paths to analyze.
     *
     * @return the input paths
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<File> input() {
        return input_;
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths the paths
     * @return this operation instance
     * @see #input(Path...)
     */
    public DetektOperation inputPaths(Collection<Path> paths) {
        return input(paths.stream().map(Path::toFile).toList());
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths the paths
     * @return this operation instance
     * @see #input(String...)
     */
    public DetektOperation inputStrings(Collection<String> paths) {
        return input(paths.stream().map(File::new).toList());
    }

    /*
     * Determines if a string is not blank.
     */
    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    /**
     * EXPERIMENTAL: Use a custom JDK home directory to include into the
     * classpath.
     *
     * @param path the JDK home directory path
     * @return this operation instance
     */
    public DetektOperation jdkHome(String path) {
        jdkHome_ = path;
        return this;
    }

    /**
     * EXPERIMENTAL: Target version of the generated JVM bytecode that was
     * generated during compilation and is now being used for type resolution
     * <p>
     * Default: 1.8
     *
     * @param target the target version
     * @return this operation instance
     */
    public DetektOperation jvmTarget(String target) {
        jvmTarget_ = target;
        return this;
    }

    /**
     * EXPERIMENTAL: Compatibility mode for Kotlin language version X.Y,
     * reports errors for all language features that came out later.
     * <p>
     * Possible Values: [1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0, 2.1]
     *
     * @param version the version
     * @return this operation instance
     */
    public DetektOperation languageVersion(String version) {
        languageVersion_ = version;
        return this;
    }

    /**
     * Return exit code 0 only when found issues count does not exceed
     * specified issues count.
     *
     * @param max the issues code
     * @return this operation instance
     */
    public DetektOperation maxIssues(int max) {
        maxIssues_ = max;
        return this;
    }

    /**
     * Enables parallel compilation and analysis of source files. Do some
     * benchmarks first before enabling this flag. Heuristics show performance
     * benefits starting from 2000 lines of Kotlin code.
     *
     * @param parallel {@code true} or {@code false}
     * @return this operation instance
     */
    public DetektOperation parallel(boolean parallel) {
        parallel_ = parallel;
        return this;
    }

    /**
     * Extra paths to plugin jars.
     *
     * @param jars one or more jars
     * @return this operation instance
     * @see #pluginsStrings(Collection)
     */
    public DetektOperation plugins(String... jars) {
        return pluginsStrings(List.of(jars));
    }

    /**
     * Extra paths to plugin jars.
     *
     * @param jars one or more jars
     * @return this operation instance
     * @see #plugins(Collection)
     */
    public DetektOperation plugins(File... jars) {
        return plugins(List.of(jars));
    }

    /**
     * Extra paths to plugin jars.
     *
     * @param jars one or more jars
     * @return this operation instance
     * @see #pluginsPaths(Collection)
     */
    public DetektOperation plugins(Path... jars) {
        return pluginsPaths(List.of(jars));
    }

    /**
     * Extra paths to plugin jars.
     *
     * @param jars the jars paths
     * @return this operation instance
     * @see #input(File...)
     */
    public DetektOperation plugins(Collection<File> jars) {
        plugins_.addAll(jars);
        return this;
    }

    /**
     * Extra path to plugins jars.
     *
     * @return the jars paths
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<File> plugins() {
        return plugins_;
    }

    /**
     * Extra paths to plugin jars.
     *
     * @param jars the jars paths
     * @return this operation instance
     * @see #plugins(Path...)
     */
    public DetektOperation pluginsPaths(Collection<Path> jars) {
        return plugins(jars.stream().map(Path::toFile).toList());
    }

    /**
     * Extra paths to plugin jars.
     *
     * @param jars the jars paths
     * @return this operation instance
     * @see #plugins(String...)
     */
    public DetektOperation pluginsStrings(Collection<String> jars) {
        return plugins(jars.stream().map(File::new).toList());
    }

    /**
     * Generates a report for given {@link ReportId report-id} and stores it on given 'path'.
     *
     * @param reports one or more reports
     * @return this operation instance
     */
    public DetektOperation report(Report... reports) {
        report_.addAll(List.of(reports));
        return this;
    }
}
