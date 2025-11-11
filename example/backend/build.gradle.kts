plugins {
    id("multiplatform")
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":example:shared"))
            implementation(project(":core"))
            implementation(project(":server"))

            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.websockets)
            implementation(libs.ktor.server.netty)
            implementation(libs.logback.classic)
            implementation(libs.ktor.server.config.yaml)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.koin.ktor)
            implementation(libs.kotlinx.rpc.krpc.server)
            implementation(libs.kotlinx.rpc.krpc.ktor.server)
            implementation(libs.kotlinx.rpc.krpc.serialization.cbor)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.sqlx4k.sqlite)

        }
    }
}

ksp {
    arg("output-package", "at.quickme.ksync.example.codegen2")
}

dependencies {
    ksp(project(":codegen"))
}