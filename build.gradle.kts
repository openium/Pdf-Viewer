import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        // Gradle
        classpath(libs.plugin.gradle)

        // Kotlin
        classpath(libs.plugin.kotlin)

        // Kotlin Serialization
        classpath(libs.plugin.kotlinSerialization)

        // Compose compiler
        classpath(libs.plugin.compose)
    }
}

subprojects {
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}