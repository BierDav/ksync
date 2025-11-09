plugins {
    id("multiplatform")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":example:shared"))
            implementation(project(":core"))
            implementation(project(":client"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.material.icons.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.sqlx4k.sqlite)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.websockets)
        }

        jvmMain.dependencies {
            //implementation(compose.desktop.currentOs)
        }
    }
}

ksp {
    arg("output-package", "at.quickme.ksync.example.codegen")
}

dependencies {
    add("kspCommonMainMetadata",project(":codegen"))
}


compose.desktop {
    application {
        mainClass = "at.quickme.ksync.example.MainKt"
    }
}