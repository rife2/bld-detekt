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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import rife.bld.BaseProject;
import rife.bld.extension.detekt.Report;
import rife.bld.extension.detekt.ReportId;
import rife.bld.extension.tools.CollectionTools;
import rife.bld.extension.tools.ObjectTools;
import rife.bld.extension.tools.TextTools;
import rife.bld.operations.AbstractProcessOperation;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "Builder pattern intentionally exposes mutable collections"
)
public class DetektOperation extends AbstractProcessOperation<DetektOperation> {

    private static final String ARG_CREATE_BASELINE = "--create-baseline";
    private static final String BASELINE = "baseline";
    private static final String CLASS_PATH = "classPath";
    private static final String CONFIG = "config";
    // Detekt jars without version numbers
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
    private static final String INPUT = "input";
    private static final String PLUGINS = "plugins";
    private static final Logger logger = Logger.getLogger(DetektOperation.class.getName());
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
    private String detektClassPathJars_;
    private boolean disableDefaultRuleSets_;
    private boolean generateConfig_;
    private String jdkHome_;
    private String jvmTarget_;
    private String languageVersion_;
    private int maxIssues_;
    private boolean parallel_;
    private BaseProject project_;

    /**
     * Performs the operation.
     *
     * @throws InterruptedException when the operation was interrupted
     * @throws IOException          when an exception occurred during the execution of the process
     * @throws ExitStatusException  when the exit status was changed during the operation
     * @throws NullPointerException if {@code project} is {@code null}
     */
    @Override
    public void execute() throws IOException, InterruptedException, ExitStatusException {
        ObjectTools.requireNonNull(project_, "project");
        if (TextTools.isBlank(detektClassPathJars_)) {
            if (logger.isLoggable(Level.SEVERE) && !silent()) {
                logger.severe("No Detekt JARs found in: " + project_.libBldDirectory());
            }
            throw new ExitStatusException(ExitStatusException.EXIT_FAILURE);
        }

        if (logger.isLoggable(Level.INFO) && !silent()) {
            if (createBaseline_) {
                ObjectTools.requireNonNull(baseline_, BASELINE);
                logger.info("Generating detekt baseline...");
            } else {
                logger.info("Running detekt analysis...");
            }
        }
        super.execute();
        if (successful_ && logger.isLoggable(Level.INFO) && !silent()) {
            if (createBaseline_) {
                logger.info("Detekt baseline generated successfully: "
                        + "file://" + new File(baseline_).toURI().getPath());
            } else {
                logger.info("Detekt operation finished successfully.");
            }
        }
    }

    /**
     * Part of the {@link #execute} operation, constructs the command list
     * to use for building the process.
     */
    @Override
    protected List<String> executeConstructProcessCommandList() {
        final List<String> args = new ArrayList<>(50); // ~2 args per option, ~25 options
        if (project_ != null) {
            args.add(javaTool());
            args.add("-cp");
            args.add(detektClassPathJars_);
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
            if (TextTools.isNotBlank(basePath_)) {
                args.add("--base-path");
                args.add(basePath_);
            }

            // baseline
            if (TextTools.isNotBlank(baseline_)) {
                args.add("--baseline");
                args.add(baseline_);
            }

            // build-upon-default-config
            if (buildUponDefaultConfig_) {
                args.add("--build-upon-default-config");
            }

            // classpath - single flag, pathSeparator joined
            if (!classpath_.isEmpty()) {
                args.add("--classpath");
                args.add(String.join(File.pathSeparator, classpath_.stream().map(File::getAbsolutePath).toList()));
            }

            // config - single flag, semicolon joined
            if (!config_.isEmpty()) {
                args.add("--config");
                args.add(String.join(";", config_.stream().map(File::getAbsolutePath).toList()));
            }

            // config-resource
            if (TextTools.isNotBlank(configResource_)) {
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

            // excludes - single flag, comma joined
            if (!excludes_.isEmpty()) {
                args.add("--excludes");
                args.add(String.join(",", excludes_));
            }

            // generate-config
            if (generateConfig_) {
                args.add("--generate-config");
            }

            // includes - single flag, comma joined
            if (!includes_.isEmpty()) {
                args.add("--includes");
                args.add(String.join(",", includes_));
            }

            // input - repeatable
            if (!input_.isEmpty()) {
                for (File f : input_) {
                    args.add("--input");
                    args.add(f.getAbsolutePath());
                }
            }

            // jdk-home
            if (TextTools.isNotBlank(jdkHome_)) {
                args.add("--jdk-home");
                args.add(jdkHome_);
            }

            // jvm-target
            if (TextTools.isNotBlank(jvmTarget_)) {
                args.add("--jvm-target");
                args.add(jvmTarget_);
            }

            // language-version
            if (TextTools.isNotBlank(languageVersion_)) {
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

            // plugins - repeatable
            if (!plugins_.isEmpty()) {
                for (File f : plugins_) {
                    args.add("--plugins");
                    args.add(f.getAbsolutePath());
                }
            }

            // report - repeatable
            if (!report_.isEmpty()) {
                report_.forEach(it -> {
                    args.add("--report");
                    args.add(it.id().name().toLowerCase() + ":" + it.path());
                });
            }

            if (logger.isLoggable(Level.FINE) && !silent()) {
                logger.fine(String.join(" ", args));
            }

            // Switch to @argfile if command line would be too long for Windows
            int totalLen = args.stream().mapToInt(String::length).sum() + args.size() - 1;
            if (totalLen > 30000 && args.size() > 1) {
                try {
                    var javaCmd = args.get(0);
                    var argFileArgs = args.subList(1, args.size());

                    var argFile = Files.createTempFile("detekt-", ".args");
                    var lines = argFileArgs.stream()
                            .map(s -> s.contains(" ") ? "\"" + s.replace("\"", "\\\"") + "\"" : s)
                            .toList();
                    Files.write(argFile, lines);
                    argFile.toFile().deleteOnExit();

                    if (logger.isLoggable(Level.FINE) && !silent()) {
                        logger.fine("Using @argfile: " + argFile + " for command length " + totalLen);
                    }

                    return List.of(javaCmd, "@" + argFile.toAbsolutePath());
                } catch (IOException e) {
                    if (logger.isLoggable(Level.WARNING) && !silent()) {
                        logger.log(Level.WARNING,
                                "Failed to create @argfile, falling back to long command: " + e.getLocalizedMessage(),
                                e);
                    }
                    // fall through and return full args
                }
            }
        }

        return args;
    }

    /**
     * Configures the operation from a {@link BaseProject}.
     * <p>
     * Sets the following:
     * <ul>
     *     <li>The {@link #baseline baseline} to {@code detekt-baseline.xml}, if it exists in the
     *     project's work directory</li>
     *     <li>The {@link #excludes excludes} to exclude {@code build} and {@code resources} directories,
     *     if not already set</li>
     *     <li>The Detekt classpath from JARS in the {@link BaseProject#libBldDirectory() project's bld lib
     *     directory}</li>
     * </ul>
     *
     * @param project the project to configure the operation from
     * @return this operation instance
     * @throws NullPointerException if {@code project} is {@code null}
     */

    @Override
    public DetektOperation fromProject(@NonNull BaseProject project) {
        project_ = ObjectTools.requireNonNull(project, "fromProject");
        var baseline = new File(project.workDirectory(), "detekt-baseline.xml");
        if (baseline.exists()) {
            baseline_ = baseline.getAbsolutePath();
        }
        if (excludes_.isEmpty()) {
            excludes(".*/build/.*", ".*/resources/.*");
        }
        detektClassPathJars_ = getDetektJarList(project_.libBldDirectory());

        parseArguments(project_.arguments());

        return this;
    }

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
     * @throws NullPointerException     if {@code path} is {@code null}
     * @throws IllegalArgumentException if {@code path} is empty
     * @see #basePath(File)
     * @see #basePath(Path)
     * @see #basePath()
     */
    public DetektOperation basePath(@NonNull String path) {
        basePath_ = ObjectTools.requireNotEmpty(path, "basePath");
        return this;
    }

    /**
     * Specifies a directory as the base path. Currently, it impacts all file
     * paths in the formatted reports. File paths in console output and txt
     * report are not affected and remain as absolute paths.
     *
     * @param path the directory path
     * @return this operation instance
     * @throws NullPointerException if {@code path} is {@code null}
     * @see #basePath(String)
     * @see #basePath(Path)
     * @see #basePath()
     */
    public DetektOperation basePath(@NonNull File path) {
        ObjectTools.requireNonNull(path, "basePath");
        return basePath(path.getAbsolutePath());
    }

    /**
     * Retrieves the base path.
     *
     * @return the directory path
     * @see #basePath(String)
     * @see #basePath(File)
     * @see #basePath(Path)
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
     * @throws NullPointerException if {@code path} is {@code null}
     * @see #basePath(String)
     * @see #basePath(File)
     * @see #basePath()
     */
    public DetektOperation basePath(@NonNull Path path) {
        ObjectTools.requireNonNull(path, "basePath");
        return basePath(path.toFile().getAbsolutePath());
    }

    /**
     * If a baseline XML file is passed in, only new code smells not in the
     * baseline are printed in the console.
     *
     * @param baseline the baseline XML file
     * @return this operation instance
     * @throws NullPointerException     if {@code baseline} is {@code null}
     * @throws IllegalArgumentException if {@code baseline} is empty
     * @see #baseline(File)
     * @see #baseline(Path)
     * @see #baseline()
     */
    public DetektOperation baseline(@NonNull String baseline) {
        baseline_ = ObjectTools.requireNotEmpty(baseline, BASELINE);
        return this;
    }

    /**
     * If a baseline XML file is passed in, only new code smells not in the
     * baseline are printed in the console.
     *
     * @param baseline the baseline XML file
     * @return this operation instance
     * @throws NullPointerException if {@code baseline} is {@code null}
     * @see #baseline(String)
     * @see #baseline(Path)
     * @see #baseline()
     */
    public DetektOperation baseline(@NonNull File baseline) {
        ObjectTools.requireNonNull(baseline, BASELINE);
        return baseline(baseline.getAbsolutePath());
    }

    /**
     * If a baseline XML file is passed in, only new code smells not in the
     * baseline are printed in the console.
     *
     * @param baseline the baseline XML file
     * @return this operation instance
     * @throws NullPointerException if {@code baseline} is {@code null}
     * @see #baseline(String)
     * @see #baseline(File)
     * @see #baseline()
     */
    public DetektOperation baseline(@NonNull Path baseline) {
        ObjectTools.requireNonNull(baseline, BASELINE);
        return baseline(baseline.toFile().getAbsolutePath());
    }

    /**
     * Retrieves the baseline XML file.
     *
     * @return the baseline XML file
     * @see #baseline(String)
     * @see #baseline(File)
     * @see #baseline(Path)
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
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty
     * @see #classPath(Path...)
     * @see #classPath(String...)
     * @see #classPath(Collection)
     * @see #classPathPaths(Collection)
     * @see #classPathStrings(Collection)
     * @see #classPath()
     */
    public DetektOperation classPath(@NonNull File... paths) {
        ObjectTools.requireNotEmpty(paths, CLASS_PATH);
        classpath_.addAll(List.of(paths));
        return this;
    }

    /**
     * EXPERIMENTAL: Paths where to find user class files and jar dependencies.
     * Used for type resolution.
     *
     * @param paths one or more files
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty
     * @see #classPath(File...)
     * @see #classPath(String...)
     * @see #classPath(Collection)
     * @see #classPathPaths(Collection)
     * @see #classPathStrings(Collection)
     * @see #classPath()
     */
    public DetektOperation classPath(@NonNull Path... paths) {
        ObjectTools.requireNotEmpty(paths, CLASS_PATH);
        classpath_.addAll(CollectionTools.combinePathsToFiles(paths));
        return this;
    }

    /**
     * EXPERIMENTAL: Paths where to find user class files and jar dependencies.
     * Used for type resolution.
     *
     * @param paths one or more files
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty or contains empty elements
     * @see #classPath(File...)
     * @see #classPath(Path...)
     * @see #classPath(Collection)
     * @see #classPathPaths(Collection)
     * @see #classPathStrings(Collection)
     * @see #classPath()
     */
    public DetektOperation classPath(@NonNull String... paths) {
        ObjectTools.requireNotEmpty(paths, CLASS_PATH);
        classpath_.addAll(CollectionTools.combineStringsToFiles(paths));
        return this;
    }

    /**
     * EXPERIMENTAL: Paths where to find user class files and jar dependencies.
     * Used for type resolution.
     *
     * @param paths the paths
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty
     * @see #classPath(File...)
     * @see #classPath(Path...)
     * @see #classPath(String...)
     * @see #classPathPaths(Collection)
     * @see #classPathStrings(Collection)
     * @see #classPath()
     */
    public final DetektOperation classPath(@NonNull Collection<File> paths) {
        ObjectTools.requireNotEmpty(paths, CLASS_PATH);
        classpath_.addAll(paths);
        return this;
    }

    /**
     * Paths where to find user class files and jar dependencies.
     *
     * @return the paths
     */
    public List<File> classPath() {
        return classpath_;
    }

    /**
     * EXPERIMENTAL: Paths where to find user class files and jar dependencies.
     * Used for type resolution.
     *
     * @param paths the paths
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty
     * @see #classPath(File...)
     * @see #classPath(Path...)
     * @see #classPath(String...)
     * @see #classPath(Collection)
     * @see #classPathStrings(Collection)
     * @see #classPath()
     */
    public final DetektOperation classPathPaths(@NonNull Collection<Path> paths) {
        ObjectTools.requireNotEmpty(paths, "classPathPaths");
        classpath_.addAll(CollectionTools.combinePathsToFiles(paths));
        return this;
    }

    /**
     * EXPERIMENTAL: Paths where to find user class files and jar dependencies.
     * Used for type resolution.
     *
     * @param paths the paths
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty or contains empty elements
     * @see #classPath(File...)
     * @see #classPath(Path...)
     * @see #classPath(String...)
     * @see #classPath(Collection)
     * @see #classPathPaths(Collection)
     * @see #classPath()
     */
    public final DetektOperation classPathStrings(@NonNull Collection<String> paths) {
        ObjectTools.requireNotEmpty(paths, "classPathStrings");
        classpath_.addAll(CollectionTools.combineStringsToFiles(paths));
        return this;
    }

    /**
     * Paths to the config files ({@code path/to/config.yml}).
     *
     * @param configs one or more config files
     * @return this operation instance
     * @throws NullPointerException     if {@code configs} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code configs} is empty
     * @see #config(Path...)
     * @see #config(String...)
     * @see #config(Collection)
     * @see #configPaths(Collection)
     * @see #configStrings(Collection)
     * @see #config()
     */
    public DetektOperation config(@NonNull File... configs) {
        ObjectTools.requireNotEmpty(configs, CONFIG);
        config_.addAll(List.of(configs));
        return this;
    }

    /**
     * Paths to the config files ({@code path/to/config.yml}).
     *
     * @param configs one or more config files
     * @return this operation instance
     * @throws NullPointerException     if {@code configs} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code configs} is empty
     * @see #config(File...)
     * @see #config(String...)
     * @see #config(Collection)
     * @see #configPaths(Collection)
     * @see #configStrings(Collection)
     * @see #config()
     */
    public DetektOperation config(@NonNull Path... configs) {
        ObjectTools.requireNotEmpty(configs, CONFIG);
        config_.addAll(CollectionTools.combinePathsToFiles(configs));
        return this;
    }

    /**
     * Paths to the config files ({@code path/to/config.yml}).
     *
     * @param configs one or more config files
     * @return this operation instance
     * @throws NullPointerException     if {@code configs} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code configs} is empty or contains empty elements
     * @see #config(File...)
     * @see #config(Path...)
     * @see #config(Collection)
     * @see #configPaths(Collection)
     * @see #configStrings(Collection)
     * @see #config()
     */
    public DetektOperation config(@NonNull String... configs) {
        ObjectTools.requireNotEmpty(configs, CONFIG);
        config_.addAll(CollectionTools.combineStringsToFiles(configs));
        return this;
    }

    /**
     * Paths to the config files ({@code path/to/config.yml}).
     *
     * @param configs the config files
     * @return this operation instance
     * @throws NullPointerException     if {@code configs} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code configs} is empty
     * @see #config(File...)
     * @see #config(Path...)
     * @see #config(String...)
     * @see #configPaths(Collection)
     * @see #configStrings(Collection)
     * @see #config()
     */
    public final DetektOperation config(@NonNull Collection<File> configs) {
        ObjectTools.requireNotEmpty(configs, CONFIG);
        config_.addAll(configs);
        return this;
    }

    /**
     * Paths to config files.
     *
     * @return the config files paths.
     */
    public List<File> config() {
        return config_;
    }

    /**
     * Paths to the config files ({@code path/to/config.yml}).
     *
     * @param configs the config files
     * @return this operation instance
     * @throws NullPointerException     if {@code configs} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code configs} is empty
     * @see #config(File...)
     * @see #config(Path...)
     * @see #config(String...)
     * @see #config(Collection)
     * @see #configStrings(Collection)
     * @see #config()
     */
    public final DetektOperation configPaths(@NonNull Collection<Path> configs) {
        ObjectTools.requireNotEmpty(configs, "configPaths");
        config_.addAll(CollectionTools.combinePathsToFiles(configs));
        return this;
    }

    /**
     * Path to the config resource on detekt's classpath ({@code path/to/config.yml}).
     *
     * @param resource the config resource path
     * @return this operation instance
     * @throws NullPointerException     if {@code resource} is {@code null}
     * @throws IllegalArgumentException if {@code resource} is empty
     * @see #configResource(File)
     * @see #configResource(Path)
     * @see #configResource()
     */
    public DetektOperation configResource(@NonNull String resource) {
        configResource_ = ObjectTools.requireNotEmpty(resource, "configResource");
        return this;
    }

    /**
     * Path to the config resource on detekt's classpath ({@code path/to/config.yml}).
     *
     * @param resource the config resource path
     * @return this operation instance
     * @throws NullPointerException if {@code resource} is {@code null}
     * @see #configResource(String)
     * @see #configResource(Path)
     * @see #configResource()
     */
    public DetektOperation configResource(@NonNull File resource) {
        ObjectTools.requireNonNull(resource, "configResource");
        return configResource(resource.getAbsolutePath());
    }

    /**
     * Path to the config resource on detekt's classpath ({@code path/to/config.yml}).
     *
     * @param resource the config resource path
     * @return this operation instance
     * @throws NullPointerException if {@code resource} is {@code null}
     * @see #configResource(String)
     * @see #configResource(File)
     * @see #configResource()
     */
    public DetektOperation configResource(@NonNull Path resource) {
        ObjectTools.requireNonNull(resource, "configResource");
        return configResource(resource.toFile().getAbsolutePath());
    }

    /**
     * Retrieves the path of the config resource.
     *
     * @return the config resource path
     * @see #configResource(String)
     * @see #configResource(File)
     * @see #configResource(Path)
     */
    public String configResource() {
        return configResource_;
    }

    /**
     * Paths to the config files ({@code path/to/config.yml}).
     *
     * @param configs the config files
     * @return this operation instance
     * @throws NullPointerException     if {@code configs} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code configs} is empty or contains empty elements
     * @see #config(File...)
     * @see #config(Path...)
     * @see #config(String...)
     * @see #config(Collection)
     * @see #configPaths(Collection)
     * @see #config()
     */
    public final DetektOperation configStrings(@NonNull Collection<String> configs) {
        ObjectTools.requireNotEmpty(configs, "configStrings");
        config_.addAll(CollectionTools.combineStringsToFiles(configs));
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
     * @throws NullPointerException     if {@code patterns} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code patterns} is empty or contains empty elements
     * @see #excludes(Collection)
     * @see #excludes()
     */
    public DetektOperation excludes(@NonNull String... patterns) {
        ObjectTools.requireNotEmpty(patterns, "excludes");
        excludes_.addAll(List.of(patterns));
        return this;
    }

    /**
     * Globbing patterns describing paths to exclude from the analysis.
     *
     * @param patterns a collection of patterns
     * @return this operation instance
     * @throws NullPointerException     if {@code patterns} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code patterns} is empty or contains empty elements
     * @see #excludes(String...)
     * @see #excludes()
     */
    public final DetektOperation excludes(@NonNull Collection<String> patterns) {
        ObjectTools.requireNotEmpty(patterns, "excludes");
        excludes_.addAll(patterns);
        return this;
    }

    /**
     * Returns the globbing patterns describing paths to exclude from the analysis.
     *
     * @return the globbing patterns
     */
    public List<String> excludes() {
        return excludes_;
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
     * combination with {@link #excludes() excludes} patterns.
     *
     * @param patterns one or more patterns
     * @return this operation instance
     * @throws NullPointerException     if {@code patterns} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code patterns} is empty or contains empty elements
     * @see #includes(Collection)
     * @see #includes()
     */
    public DetektOperation includes(@NonNull String... patterns) {
        ObjectTools.requireNotEmpty(patterns, "includes");
        includes_.addAll(List.of(patterns));
        return this;
    }

    /**
     * Globbing patterns describing paths to include in the analysis. Useful in
     * combination with {@link #excludes() excludes} patterns.
     *
     * @param patterns a collection of patterns
     * @return this operation instance
     * @throws NullPointerException     if {@code patterns} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code patterns} is empty or contains empty elements
     * @see #includes(String...)
     * @see #includes()
     */
    public final DetektOperation includes(@NonNull Collection<String> patterns) {
        ObjectTools.requireNotEmpty(patterns, "includes");
        includes_.addAll(patterns);
        return this;
    }

    /**
     * Returns the globbing patterns describing paths to include in the analysis.
     *
     * @return the globbing patterns
     */
    public List<String> includes() {
        return includes_;
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths the paths
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty
     * @see #input(File...)
     * @see #input(Path...)
     * @see #input(String...)
     * @see #inputPaths(Collection)
     * @see #inputStrings(Collection)
     * @see #input()
     */
    public final DetektOperation input(@NonNull Collection<File> paths) {
        ObjectTools.requireNotEmpty(paths, INPUT);
        input_.addAll(paths);
        return this;
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths one or more paths
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty or contains empty elements
     * @see #input(File...)
     * @see #input(Path...)
     * @see #input(Collection)
     * @see #inputPaths(Collection)
     * @see #inputStrings(Collection)
     * @see #input()
     */
    public DetektOperation input(@NonNull String... paths) {
        ObjectTools.requireNotEmpty(paths, INPUT);
        input_.addAll(CollectionTools.combineStringsToFiles(paths));
        return this;
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths one or more paths
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty
     * @see #input(Path...)
     * @see #input(String...)
     * @see #input(Collection)
     * @see #inputPaths(Collection)
     * @see #inputStrings(Collection)
     * @see #input()
     */
    public DetektOperation input(@NonNull File... paths) {
        ObjectTools.requireNotEmpty(paths, INPUT);
        input_.addAll(List.of(paths));
        return this;
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths one or more paths
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty
     * @see #input(File...)
     * @see #input(String...)
     * @see #input(Collection)
     * @see #inputPaths(Collection)
     * @see #inputStrings(Collection)
     * @see #input()
     */
    public DetektOperation input(@NonNull Path... paths) {
        ObjectTools.requireNotEmpty(paths, INPUT);
        input_.addAll(CollectionTools.combinePathsToFiles(paths));
        return this;
    }

    /**
     * Returns the input paths to analyze.
     *
     * @return the input paths
     */
    public List<File> input() {
        return input_;
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths the paths
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty
     * @see #input(File...)
     * @see #input(Path...)
     * @see #input(String...)
     * @see #input(Collection)
     * @see #inputStrings(Collection)
     * @see #input()
     */
    public final DetektOperation inputPaths(@NonNull Collection<Path> paths) {
        ObjectTools.requireNotEmpty(paths, "inputPaths");
        input_.addAll(CollectionTools.combinePathsToFiles(paths));
        return this;
    }

    /**
     * Input paths to analyze. If not specified the current working directory is used.
     *
     * @param paths the paths
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty or contains empty elements
     * @see #input(File...)
     * @see #input(Path...)
     * @see #input(String...)
     * @see #input(Collection)
     * @see #inputPaths(Collection)
     * @see #input()
     */
    public final DetektOperation inputStrings(@NonNull Collection<String> paths) {
        ObjectTools.requireNotEmpty(paths, "inputStrings");
        input_.addAll(CollectionTools.combineStringsToFiles(paths));
        return this;
    }

    /**
     * EXPERIMENTAL: Use a custom JDK home directory to include into the
     * classpath.
     *
     * @param path the JDK home directory path
     * @return this operation instance
     * @throws NullPointerException     if {@code path} is {@code null}
     * @throws IllegalArgumentException if {@code path} is blank
     */
    public DetektOperation jdkHome(@NonNull String path) {
        jdkHome_ = TextTools.requireNotBlank(path, "jdkHome");
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
     * @throws NullPointerException     if {@code target} is {@code null}
     * @throws IllegalArgumentException if {@code target} is blank
     */
    public DetektOperation jvmTarget(@NonNull String target) {
        jvmTarget_ = TextTools.requireNotBlank(target, "jvmTarget");
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
     * @throws NullPointerException     if {@code version} is {@code null}
     * @throws IllegalArgumentException if {@code version} is blank
     */
    public DetektOperation languageVersion(@NonNull String version) {
        languageVersion_ = TextTools.requireNotBlank(version, "languageVersion");
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
     * @throws NullPointerException     if {@code jars} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code jars} is empty or contains empty elements
     * @see #plugins(File...)
     * @see #plugins(Path...)
     * @see #plugins(Collection)
     * @see #pluginsPaths(Collection)
     * @see #pluginsStrings(Collection)
     * @see #plugins()
     */
    public DetektOperation plugins(@NonNull String... jars) {
        ObjectTools.requireNotEmpty(jars, PLUGINS);
        plugins_.addAll(CollectionTools.combineStringsToFiles(jars));
        return this;
    }

    /**
     * Extra paths to plugin jars.
     *
     * @param jars one or more jars
     * @return this operation instance
     * @throws NullPointerException     if {@code jars} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code jars} is empty
     * @see #plugins(Path...)
     * @see #plugins(String...)
     * @see #plugins(Collection)
     * @see #pluginsPaths(Collection)
     * @see #pluginsStrings(Collection)
     * @see #plugins()
     */
    public DetektOperation plugins(@NonNull File... jars) {
        ObjectTools.requireNotEmpty(jars, PLUGINS);
        plugins_.addAll(List.of(jars));
        return this;
    }

    /**
     * Extra paths to plugin jars.
     *
     * @param jars one or more jars
     * @return this operation instance
     * @throws NullPointerException     if {@code jars} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code jars} is empty
     * @see #plugins(File...)
     * @see #plugins(String...)
     * @see #plugins(Collection)
     * @see #pluginsPaths(Collection)
     * @see #pluginsStrings(Collection)
     * @see #plugins()
     */
    public DetektOperation plugins(@NonNull Path... jars) {
        ObjectTools.requireNotEmpty(jars, PLUGINS);
        plugins_.addAll(CollectionTools.combinePathsToFiles(jars));
        return this;
    }

    /**
     * Extra paths to plugin jars.
     *
     * @param jars the jars paths
     * @return this operation instance
     * @throws NullPointerException     if {@code jars} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code jars} is empty
     * @see #plugins(File...)
     * @see #plugins(Path...)
     * @see #plugins(String...)
     * @see #pluginsPaths(Collection)
     * @see #pluginsStrings(Collection)
     * @see #plugins()
     */
    public final DetektOperation plugins(@NonNull Collection<File> jars) {
        ObjectTools.requireNotEmpty(jars, PLUGINS);
        plugins_.addAll(jars);
        return this;
    }

    /**
     * Extra path to plugins jars.
     *
     * @return the jars paths
     */
    public List<File> plugins() {
        return plugins_;
    }

    /**
     * Extra paths to plugin jars.
     *
     * @param jars the jars paths
     * @return this operation instance
     * @throws NullPointerException     if {@code jars} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code jars} is empty
     * @see #plugins(File...)
     * @see #plugins(Path...)
     * @see #plugins(String...)
     * @see #plugins(Collection)
     * @see #pluginsStrings(Collection)
     * @see #plugins()
     */
    public final DetektOperation pluginsPaths(@NonNull Collection<Path> jars) {
        ObjectTools.requireNotEmpty(jars, "pluginsPaths");
        plugins_.addAll(CollectionTools.combinePathsToFiles(jars));
        return this;
    }

    /**
     * Extra paths to plugin jars.
     *
     * @param jars the jars paths
     * @return this operation instance
     * @throws NullPointerException     if {@code jars} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code jars} is empty or contains empty elements
     * @see #plugins(File...)
     * @see #plugins(Path...)
     * @see #plugins(String...)
     * @see #plugins(Collection)
     * @see #pluginsPaths(Collection)
     * @see #plugins()
     */
    public final DetektOperation pluginsStrings(@NonNull Collection<String> jars) {
        ObjectTools.requireNotEmpty(jars, "pluginsStrings");
        plugins_.addAll(CollectionTools.combineStringsToFiles(jars));
        return this;
    }

    /**
     * Generates a report for given {@link ReportId report-id} and stores it on given 'path'.
     *
     * @param reports one or more reports
     * @return this operation instance
     * @throws NullPointerException     if {@code reports} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code reports} is empty
     */
    public DetektOperation report(@NonNull Report... reports) {
        ObjectTools.requireNotEmpty(reports, "report");
        report_.addAll(List.of(reports));
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

    private void parseArguments(List<String> args) {
        if (args.isEmpty()) {
            return;
        }

        var arg = args.get(0);
        if (ARG_CREATE_BASELINE.equals(arg)) {
            createBaseline_ = true;
            args.remove(0);
        }
    }
}
