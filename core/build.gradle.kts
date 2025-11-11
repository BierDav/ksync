plugins {
    id("multiplatform")
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlinx.rpc)
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
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.rpc.core)
        }
    }
}
