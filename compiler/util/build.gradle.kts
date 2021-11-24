plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(kotlinStdlib())
    api(project(":compiler:compiler.version"))

    compileOnly(intellijCore())
    compileOnly(commonDependency("org.jetbrains.intellij.deps:log4j"))
    compileOnly(commonDependency("org.jetbrains.intellij.deps:asm-all"))
    compileOnly(jpsModel())
    compileOnly(jpsModelImpl())
}

sourceSets {
    "main" {
        projectDefault()
        resources.srcDir(File(rootDir, "resources"))
    }
    "test" {}
}
