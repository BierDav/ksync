plugins {
    id("multiplatform")
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":example:shared"))
            implementation(project(":core"))
            implementation(project(":server"))

            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.websockets)
            implementation(libs.ktor.server.netty)
            implementation(libs.logback.classic)
            implementation(libs.ktor.server.config.yaml)
            implementation(libs.koin.ktor)
            implementation(libs.sqlx4k.sqlite)
        }
    }
}

ksp {
    arg("output-package", "at.quickme.ksync.example.codegen")
}

dependencies {
    add("kspCommonMainMetadata",project(":codegen"))
}