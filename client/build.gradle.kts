plugins {
    id("multiplatform")
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
        }
    }
}
