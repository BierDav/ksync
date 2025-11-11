plugins {
    id("multiplatform")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
//    alias(libs.plugins.composeHotReload)
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
            implementation(project(":client"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.desktop.currentOs)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.material.icons.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.sqlx4k.sqlite)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.rpc.krpc.client)
            implementation(libs.kotlinx.rpc.krpc.ktor.client)
            implementation(libs.kotlinx.rpc.krpc.serialization.cbor)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

ksp {
    arg("output-package", "at.quickme.ksync.example.codegen2")
}

dependencies {
    ksp(project(":codegen"))
}


compose.desktop {
    application {
        mainClass = "at.quickme.ksync.example.ApplicationKt"
    }
}