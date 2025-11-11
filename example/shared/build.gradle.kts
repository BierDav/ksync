@file:OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("multiplatform")
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.rpc)
}

kotlin {
    wasmJs {
        browser()
        nodejs()
    }
    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
        commonMain.dependencies {
            implementation(project(":core"))

            implementation(libs.kotlinx.rpc.core)
            implementation(libs.koin.core)
            implementation(libs.sqlx4k)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

ksp {
    arg("output-package", "at.quickme.ksync.example.codegen")
}

dependencies {
    add("kspCommonMainMetadata", project(":codegen"))
}

project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}