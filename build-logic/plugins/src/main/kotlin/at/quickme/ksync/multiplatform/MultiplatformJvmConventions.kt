package at.quickme.ksync.multiplatform

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("unused")
class MultiplatformJvmConventions : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("org.jetbrains.kotlin.jvm")
    }
}
