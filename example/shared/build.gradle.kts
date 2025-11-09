import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("multiplatform")
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))

            implementation(libs.koin.core)
            implementation(libs.sqlx4k.sqlite)
            implementation(libs.kotlinx.serialization.core)
        }
    }
}

ksp {
    arg("output-package", "at.quickme.ksync.example.codegen")
}

dependencies {
    add("kspCommonMainMetadata", project(":codegen"))
}

//project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
//    if (name != "kspCommonMainKotlinMetadata") {
//        dependsOn("kspCommonMainKotlinMetadata")
//    }
//}