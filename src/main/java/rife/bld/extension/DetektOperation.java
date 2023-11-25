/*
 * Copyright 2023 the original author or authors.
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

import rife.bld.BaseProject;
import rife.bld.operations.AbstractProcessOperation;

import java.io.File;
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
    private static final Logger LOGGER = Logger.getLogger(DetektReport.class.getName());
    private final Collection<String> classpath_ = new ArrayList<>();
    private final Collection<String> config_ = new ArrayList<>();
    private final Collection<String> input_ = new ArrayList<>();
    private final Collection<String> plugins_ = new ArrayList<>();
    private final Collection<DetektReport> report_ = new ArrayList<>();
    private boolean allRules_ = false;
    private boolean autoCorrect_ = false;
    private String basePath_;
    private String baseline_;
    private boolean buildUponDefaultConfig_;
    private String configResource_;
    private boolean createBaseline_;
    private boolean debug_ = false;
    private boolean disableDefaultRuleSets_ = false;
    private String excludes_;
    private boolean generateConfig_;
    private String includes_;
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
     * Allow rules to auto correct code if they support it. The default rule
     * sets do NOT support auto correcting and won't change any line in the
     * users code base. However custom rules can be written to support auto
     * correcting. The additional 'formatting' rule set, added with
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
     * Specifies a directory as the base path. Currently it impacts all file
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
     * EXPERIMENTAL: Paths where to find user class files and depending jar
     * files. Used for type resolution.
     *
     * @param paths one or more files
     * @return this operation instance
     */
    public DetektOperation classPath(String... paths) {
        classpath_.addAll(List.of(paths));
        return this;
    }

    /**
     * EXPERIMENTAL: Paths where to find user class files and depending jar
     * files. Used for type resolution.
     *
     * @param paths the list of files
     * @return this operation instance
     */
    public DetektOperation classPath(Collection<String> paths) {
        classpath_.addAll(paths);
        return this;
    }

    /**
     * Path to the config file ({@code path/to/config.yml}).
     *
     * @param configs one or more config files
     * @return this operation instance
     */
    public DetektOperation config(String... configs) {
        config_.addAll(List.of(configs));
        return this;
    }

    /**
     * Path to the config file ({@code path/to/config.yml}).
     *
     * @param configs the list pf config files
     * @return this operation instance
     */
    public DetektOperation config(Collection<String> configs) {
        config_.addAll(configs);
        return this;
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
     * @param patterns the patterns
     * @return this operation instance
     */
    public DetektOperation excludes(String patterns) {
        excludes_ = patterns;
        return this;
    }

    /**
     * Part of the {@link #execute} operation, constructs the command list
     * to use for building the process.
     */
    @Override
    protected List<String> executeConstructProcessCommandList() {
        if (project_ == null) {
            LOGGER.severe("A project must be specified.");
        }

        final List<String> args = new ArrayList<>();
        args.add(javaTool());
        args.add("-cp");
        args.add(Path.of(project_.libBldDirectory().getAbsolutePath(), "*").toString());
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
        if (basePath_ != null) {
            args.add("--base-path");
            args.add(basePath_);
        }

        // baseline
        if (baseline_ != null) {
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
            args.add(String.join(File.pathSeparator, classpath_));
        }

        // config
        if (!config_.isEmpty()) {
            args.add("-config");
            args.add(String.join(";", config_));
        }

        // config-resource
        if (configResource_ != null) {
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
        if (excludes_ != null) {
            args.add("--excludes");

        }

        // generate-config
        if (generateConfig_) {
            args.add("--generate-config");
        }

        // includes
        if (includes_ != null) {
            args.add("--includes");
            args.add(includes_);
        }

        // input
        if (!input_.isEmpty()) {
            args.add("--input");
            args.add(String.join(",", input_));
        }

        // jdk-home
        if (jdkHome_ != null) {
            args.add("--jdk-home");
            args.add(jdkHome_);
        }

        // jvm-target
        if (jvmTarget_ != null) {
            args.add("--jvm-target");
            args.add(jvmTarget_);
        }

        // language-version
        if (languageVersion_ != null) {
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
            args.add(String.join(".", plugins_));
        }

        // report
        if (!report_.isEmpty()) {
            report_.forEach(it -> {
                args.add("-r");
                args.add(it.id().name().toLowerCase() + ":" + it.path());
            });
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.join(" ", args));
        }

        return args;
    }

    /**
     * Configures the operation from a {@link BaseProject}.
     * <p>
     * Sets the {@link #input input} to {@code src/main/kotlin}, {@code src/test/kotlin} and {@code detekt-baseline.xml}
     * if they exist.
     *
     * @param project the project to configure the operation from
     * @return this operation instance
     */
    @Override
    public DetektOperation fromProject(BaseProject project) {
        project_ = project;
        var main = new File(project.srcMainDirectory(), "kotlin");
        if (main.exists()) {
            input_.add(main.getAbsolutePath());
        }
        var test = new File(project.srcTestDirectory(), "kotlin");
        if (test.exists()) {
            input_.add(test.getAbsolutePath());
        }
        var baseline = new File(project.workDirectory(), "detekt-baseline.xml");
        if (baseline.exists()) {
            baseline_ = baseline.getAbsolutePath();
        }
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

    /**
     * Globbing patterns describing paths to include in the analysis. Useful in
     * combination with {@link #excludes(String) excludes} patterns.
     *
     * @param patterns the patterns
     * @return this operation instance
     */
    public DetektOperation includes(String patterns) {
        includes_ = patterns;
        return this;
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths the list of paths
     * @return this operation instance
     */
    public DetektOperation input(Collection<String> paths) {
        input_.addAll(paths);
        return this;
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths one or more paths
     * @return this operation instance
     */
    public DetektOperation input(String... paths) {
        input_.addAll(List.of(paths));
        return this;
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
     * @param parallel{@code true} or {@code false}
     * @return this operation instance
     */
    public DetektOperation parallel(boolean parallel) {
        parallel_ = parallel;
        return this;
    }

    /**
     * Enables parallel compilation and analysis of source files. Do some
     * benchmarks first before enabling this flag. Heuristics show performance
     * benefits starting from 2000 lines of Kotlin code.
     *
     * @param jars one or more jars
     * @return this operation instance
     */
    public DetektOperation plugins(String... jars) {
        plugins_.addAll(List.of(jars));
        return this;
    }

    /**
     * Enables parallel compilation and analysis of source files. Do some
     * benchmarks first before enabling this flag. Heuristics show performance
     * benefits starting from 2000 lines of Kotlin code.
     *
     * @param jars the list of jars
     * @return this operation instance
     */
    public DetektOperation plugins(Collection<String> jars) {
        plugins_.addAll(jars);
        return this;
    }

    /**
     * Generates a report for given 'report-id' and stores it on given 'path'.
     *
     * @param reports one or more reports
     * @return this operation instance
     */
    public DetektOperation report(DetektReport... reports) {
        report_.addAll(List.of(reports));
        return this;
    }
}