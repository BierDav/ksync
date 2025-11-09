plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("multiplatform") {
            id = "multiplatform"
            implementationClass = "at.quickme.ksync.multiplatform.MultiplatformConventions"
        }
        create("multiplatform.jvm") {
            id = "multiplatform.jvm"
            implementationClass = "at.quickme.ksync.multiplatform.MultiplatformJvmConventions"
        }
    }
}

dependencies {
    compileOnly(libs.gradle.kotlin.plugin)
}
