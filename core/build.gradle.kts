plugins {
    id("multiplatform")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")

        freeCompilerArgs.add("-Xcontext-parameters")
    }
    sourceSets {
        commonMain.dependencies {
            api(libs.sqlx4k)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
