import java.net.URI
import java.util.Properties

group = "at.quickme.ksync"
version = "0.1.0"

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
}

repositories {
    mavenCentral()
}

val localProperties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream -> localProperties.load(stream) }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        google()
        maven {
            name = "Github"
            url = URI.create("https://maven.pkg.github.com/BierDav/sqlx4k")
            credentials {
                username = localProperties.getProperty("githubActor")
                password = localProperties.getProperty("githubToken")
            }
        }
    }
}
