/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.ir

enum class KotlinJsIrCompilationOutputMode {
    WHOLE_PROGRAM,
    PER_MODULE;

    companion object {
        fun byArgument(argument: String): KotlinJsIrCompilationOutputMode? =
            KotlinJsIrCompilationOutputMode.values()
                .firstOrNull { it.name.replace("_", "-").equals(argument, ignoreCase = true) }
    }
}

fun KotlinJsIrCompilationOutputMode.toCompilerArgument(): String {
    val perModule = this == KotlinJsIrCompilationOutputMode.PER_MODULE
    return "$PER_MODULE=$perModule"
}