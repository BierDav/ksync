plugins {
    id("multiplatform")
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("kotlin.concurrent.atomics.ExperimentalAtomicApi")
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
        }
    }
}
