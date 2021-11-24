import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.pill.PillExtension

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

pill {
    variant = PillExtension.Variant.FULL
}

val kotlinGradlePluginTest = project(":kotlin-gradle-plugin").sourceSets.named("test").map { it.output }

dependencies {
    testImplementation(project(":kotlin-gradle-plugin"))
    testImplementation(project(":kotlin-gradle-plugin-model"))
    testImplementation(project(":kotlin-project-model"))
    testImplementation(project(":kotlin-tooling-metadata"))
    testImplementation(kotlinGradlePluginTest)
    testImplementation(project(":kotlin-gradle-subplugin-example"))
    testImplementation(project(":kotlin-allopen"))
    testImplementation(project(":kotlin-noarg"))
    testImplementation(project(":kotlin-lombok"))
    testImplementation(project(":kotlin-sam-with-receiver"))
    testImplementation(project(":kotlin-test:kotlin-test-jvm"))
    testImplementation(project(":native:kotlin-native-utils"))
    testImplementation(project(":native:kotlin-klib-commonizer-api"))

    testImplementation(project(":kotlin-compiler-embeddable"))
    testImplementation(commonDependency("org.jetbrains.intellij.deps:jdom"))
    // testCompileOnly dependency on non-shaded artifacts is needed for IDE support
    // testRuntimeOnly on shaded artifact is needed for running tests with shaded compiler
    testCompileOnly(project(":kotlin-gradle-plugin-test-utils-embeddable"))
    testRuntimeOnly(project(":kotlin-gradle-plugin-test-utils-embeddable"))

    testImplementation(project(path = ":examples:annotation-processor-example"))
    testImplementation(kotlinStdlib("jdk8"))
    testImplementation(project(":kotlin-reflect"))
    testImplementation(project(":kotlin-android-extensions"))
    testImplementation(project(":kotlin-parcelize-compiler"))
    testImplementation(commonDependency("org.jetbrains.intellij.deps", "trove4j"))

    testImplementation(gradleApi())
    testImplementation(gradleTestKit())
    testImplementation(commonDependency("com.google.code.gson:gson"))
    testApiJUnit5(vintageEngine = true, jupiterParams = true)

    testRuntimeOnly(project(":kotlin-android-extensions"))
    testRuntimeOnly(project(":compiler:tests-mutes"))

    // Workaround for missing transitive import of the common(project `kotlin-test-common`
    // for `kotlin-test-jvm` into the IDE:
    testCompileOnly(project(":kotlin-test:kotlin-test-common")) { isTransitive = false }
    testCompileOnly(commonDependency("org.jetbrains.intellij.deps:asm-all"))
}

// Aapt2 from Android Gradle Plugin 3.2 and below does not handle long paths on Windows.
val shortenTempRootName = project.providers.systemProperty("os.name").forUseAtConfigurationTime().get().contains("Windows")

val isTeamcityBuild = project.kotlinBuildProperties.isTeamcityBuild ||
        try {
            project.providers.gradleProperty("gradle.integration.tests.split.tasks").forUseAtConfigurationTime().orNull
                ?.toBoolean() ?: false
        } catch (_: Exception) {
            false
        }

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.io.path.ExperimentalPathApi"
}

val cleanTestKitCacheTask = tasks.register<Delete>("cleanTestKitCache") {
    group = "Build"
    description = "Deletes temporary Gradle TestKit cache"

    delete(project.file(".testKitDir"))
}

fun Test.includeMppAndAndroid(include: Boolean) = includeTestsWithPattern(include) {
    addAll(listOf("*Multiplatform*", "*Mpp*", "*Android*"))
}

fun Test.includeNative(include: Boolean) = includeTestsWithPattern(include) {
    addAll(listOf("org.jetbrains.kotlin.gradle.native.*", "*Commonizer*"))
}

fun Test.includeTestsWithPattern(include: Boolean, patterns: (MutableSet<String>).() -> Unit) {
    if (isTeamcityBuild) {
        val filter = if (include)
            filter.includePatterns
        else
            filter.excludePatterns
        filter.patterns()
    }
}

fun Test.advanceGradleVersion() {
    val gradleVersionForTests = "7.0"
    systemProperty("kotlin.gradle.version.for.tests", gradleVersionForTests)
}

// additional configuration in tasks.withType<Test> below
projectTest(
    "test",
    shortenTempRootName = shortenTempRootName,
    jUnitMode = JUnitMode.JUnit5
) {
    includeMppAndAndroid(false)
    includeNative(false)
    if (isTeamcityBuild) finalizedBy(cleanTestKitCacheTask)
}

projectTest(
    "testAdvanceGradleVersion",
    shortenTempRootName = shortenTempRootName,
    jUnitMode = JUnitMode.JUnit5
) {
    advanceGradleVersion()
    includeMppAndAndroid(false)
    includeNative(false)

    if (isTeamcityBuild) finalizedBy(cleanTestKitCacheTask)
}

if (isTeamcityBuild) {
    projectTest(
        "testNative",
        shortenTempRootName = shortenTempRootName,
        jUnitMode = JUnitMode.JUnit5
    ) {
        includeNative(true)
        finalizedBy(cleanTestKitCacheTask)
    }

    projectTest(
        "testAdvanceGradleVersionNative",
        shortenTempRootName = shortenTempRootName,
        jUnitMode = JUnitMode.JUnit5
    ) {
        advanceGradleVersion()
        includeNative(true)
        finalizedBy(cleanTestKitCacheTask)
    }

    projectTest(
        "testMppAndAndroid",
        shortenTempRootName = shortenTempRootName,
        jUnitMode = JUnitMode.JUnit5
    ) {
        includeMppAndAndroid(true)
        finalizedBy(cleanTestKitCacheTask)
    }

    projectTest(
        "testAdvanceGradleVersionMppAndAndroid",
        shortenTempRootName = shortenTempRootName,
        jUnitMode = JUnitMode.JUnit5
    ) {
        advanceGradleVersion()
        includeMppAndAndroid(true)
        finalizedBy(cleanTestKitCacheTask)
    }
}

val KGP_TEST_TASKS_GROUP = "Kotlin Gradle Plugin Verification"
val maxParallelTestForks = (Runtime.getRuntime().availableProcessors() / 4).coerceAtLeast(1)

val allParallelTestsTask = tasks.register<Test>("kgpAllParallelTests") {
    group = KGP_TEST_TASKS_GROUP
    description = "Runs all tests for Kotlin Gradle plugins except daemon ones"

    maxParallelForks = maxParallelTestForks

    useJUnitPlatform {
        excludeTags("DaemonsKGP")
        includeEngines("junit-jupiter")
    }

    if (isTeamcityBuild) finalizedBy(cleanTestKitCacheTask)
}

val simpleTestsTask = tasks.register<Test>("kgpSimpleTests") {
    group = KGP_TEST_TASKS_GROUP
    description = "Run only simple tests for Kotlin Gradle Plugin (deprecated)"
    maxParallelForks = maxParallelTestForks

    useJUnitPlatform {
        includeTags("SimpleKGP")
        includeEngines("junit-jupiter")
    }

    if (isTeamcityBuild) finalizedBy(cleanTestKitCacheTask)
}

val jvmTestsTask = tasks.register<Test>("kgpJvmTests") {
    group = KGP_TEST_TASKS_GROUP
    description = "Run tests for Kotlin/JVM part of Gradle plugin"
    maxParallelForks = maxParallelTestForks
    useJUnitPlatform {
        includeTags("JvmKGP")
        includeEngines("junit-jupiter")
    }

    if (isTeamcityBuild) finalizedBy(cleanTestKitCacheTask)
}

val jsTestsTask = tasks.register<Test>("kgpJsTests") {
    group = KGP_TEST_TASKS_GROUP
    description = "Run tests for Kotlin/JS part of Gradle plugin"
    maxParallelForks = maxParallelTestForks
    useJUnitPlatform {
        includeTags("JsKGP")
        includeEngines("junit-jupiter")
    }

    if (isTeamcityBuild) finalizedBy(cleanTestKitCacheTask)
}

// Daemon tests could run only sequentially as they could not be shared between parallel test builds
val daemonsTestsTask = tasks.register<Test>("kgpDaemonTests") {
    group = KGP_TEST_TASKS_GROUP
    description = "Run only Gradle and Kotlin daemon tests for Kotlin Gradle Plugin"
    maxParallelForks = 1

    mustRunAfter(simpleTestsTask)

    useJUnitPlatform {
        includeTags("DaemonsKGP")
        includeEngines("junit-jupiter")
    }

    // Disabled cause jna dependency FD is leaking on windows agents
    //if (isTeamcityBuild) finalizedBy(cleanTestKitCacheTask)
}

val otherPluginsTestTask = tasks.register<Test>("kgpOtherTests") {
    group = KGP_TEST_TASKS_GROUP
    description = "Run tests for all support plugins, such as kapt, allopen, etc"
    maxParallelForks = maxParallelTestForks
    useJUnitPlatform {
        includeTags("OtherKGP")
        includeEngines("junit-jupiter")
    }

    if (isTeamcityBuild) finalizedBy(cleanTestKitCacheTask)
}

val mppTestsTask = tasks.register<Test>("kgpMppTests") {
    group = KGP_TEST_TASKS_GROUP
    description = "Run Multiplatform Kotlin Gradle plugin tests"
    maxParallelForks = maxParallelTestForks
    useJUnitPlatform {
        includeTags("MppKGP")
        includeEngines("junit-jupiter")
    }

    if (isTeamcityBuild) finalizedBy(cleanTestKitCacheTask)
}

val androidTestsTask = tasks.register<Test>("kgpAndroidTests") {
    group = KGP_TEST_TASKS_GROUP
    description = "Run Android Kotlin Gradle plugin tests"
    maxParallelForks = maxParallelTestForks
    useJUnitPlatform {
        includeTags("AndroidKGP")
        includeEngines("junit-jupiter")
    }

    if (isTeamcityBuild) finalizedBy(cleanTestKitCacheTask)
}

tasks.named<Task>("check") {
    dependsOn("testAdvanceGradleVersion")
    dependsOn(simpleTestsTask, jvmTestsTask, jsTestsTask, daemonsTestsTask, otherPluginsTestTask, mppTestsTask, androidTestsTask)
    if (isTeamcityBuild) {
        dependsOn("testAdvanceGradleVersionMppAndAndroid")
        dependsOn("testMppAndAndroid")
        dependsOn("testNative")
        dependsOn("testAdvanceGradleVersionNative")
        finalizedBy(cleanTestKitCacheTask)
    }
}

tasks.withType<Test> {
    val noTestProperty = project.providers.gradleProperty("noTest")
    onlyIf { !noTestProperty.isPresent }

    dependsOn(":kotlin-gradle-plugin:validatePlugins")
    dependsOnKotlinGradlePluginInstall()
    dependsOn(":examples:annotation-processor-example:install")

    systemProperty("kotlinVersion", rootProject.extra["kotlinVersion"] as String)
    systemProperty("runnerGradleVersion", gradle.gradleVersion)

    val installCocoapods = project.findProperty("installCocoapods") as String?
    if (installCocoapods != null) {
        systemProperty("installCocoapods", installCocoapods)
    }

    val jdk8Provider = project.getToolchainLauncherFor(JdkMajorVersion.JDK_1_8).map { it.metadata.installationPath.asFile.absolutePath }
    val jdk9Provider = project.getToolchainLauncherFor(JdkMajorVersion.JDK_9).map { it.metadata.installationPath.asFile.absolutePath }
    val jdk10Provider = project.getToolchainLauncherFor(JdkMajorVersion.JDK_10).map { it.metadata.installationPath.asFile.absolutePath }
    val jdk11Provider = project.getToolchainLauncherFor(JdkMajorVersion.JDK_11).map { it.metadata.installationPath.asFile.absolutePath }
    val jdk16Provider = project.getToolchainLauncherFor(JdkMajorVersion.JDK_16).map { it.metadata.installationPath.asFile.absolutePath }
    val mavenLocalRepo = project.providers.systemProperty("maven.repo.local").forUseAtConfigurationTime().orNull

    // Query required JDKs paths only on execution phase to avoid triggering auto-download on project configuration phase
    doFirst {
        systemProperty("jdk8Home", jdk8Provider.get())
        systemProperty("jdk9Home", jdk9Provider.get())
        systemProperty("jdk10Home", jdk10Provider.get())
        systemProperty("jdk11Home", jdk11Provider.get())
        systemProperty("jdk16Home", jdk16Provider.get())
        if (mavenLocalRepo != null) {
            systemProperty("maven.repo.local", mavenLocalRepo)
        }
    }

    useAndroidSdk()

    val shouldApplyJunitPlatform = name !in setOf(
        allParallelTestsTask.name,
        simpleTestsTask.name,
        jvmTestsTask.name,
        jsTestsTask.name,
        daemonsTestsTask.name,
        otherPluginsTestTask.name,
        mppTestsTask.name,
        androidTestsTask.name
    )
    if (shouldApplyJunitPlatform) {
        maxHeapSize = "512m"
        useJUnitPlatform {
            includeEngines("junit-vintage")
        }
    }

    testLogging {
        // set options for log level LIFECYCLE
        events("passed", "skipped", "failed", "standardOut")
        showExceptions = true
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showStackTraces = true

        // set options for log level DEBUG and INFO
        debug {
            events("started", "passed", "skipped", "failed", "standardOut", "standardError")
            exceptionFormat = TestExceptionFormat.FULL
        }
        info.events = debug.events
        info.exceptionFormat = debug.exceptionFormat

        addTestListener(object : TestListener {
            override fun afterSuite(desc: TestDescriptor, result: TestResult) {
                if (desc.parent == null) { // will match the outermost suite
                    val output =
                        "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)"
                    val startItem = "|  "
                    val endItem = "  |"
                    val repeatLength = startItem.length + output.length + endItem.length
                    println("\n" + ("-".repeat(repeatLength)) + "\n" + startItem + output + endItem + "\n" + ("-".repeat(repeatLength)))
                }
            }

            override fun beforeSuite(suite: TestDescriptor) {}
            override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}
            override fun beforeTest(testDescriptor: TestDescriptor) {}
        })
    }
}
