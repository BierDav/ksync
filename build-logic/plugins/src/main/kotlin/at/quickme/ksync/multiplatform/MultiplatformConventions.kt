package at.quickme.ksync.multiplatform

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("unused")
class MultiplatformConventions : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("org.jetbrains.kotlin.multiplatform")
        project.extensions.configure<KotlinMultiplatformExtension> {
            jvm()
            linuxX64()

            applyDefaultHierarchyTemplate()
        }
    }
}
