plugins {
    id("multiplatform.jvm")
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("com.squareup.kotlinpoet.ExperimentalKotlinPoetApi")
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.ksp)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.jsqlparser)
    implementation(libs.calcite.core)
    implementation(libs.log4k.slf4j)
}