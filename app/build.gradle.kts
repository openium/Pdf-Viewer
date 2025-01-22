import org.gradle.language.nativeplatform.internal.BuildType
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlinx-serialization") // TODO
}

// Keystore
val keystorePropertiesFile = rootProject.file("keys/keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
    namespace = "fr.openium.kodex.sample.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "fr.openium.kodex.sample.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        named(BuildType.DEBUG.name) {
            storeFile = file(keystoreProperties["debugStoreFile"].toString())
        }
        register(BuildType.RELEASE.name) {
            storeFile = file(keystoreProperties["releaseStoreFile"].toString())
            storePassword = keystoreProperties["passwordRelease"].toString()
            keyAlias = keystoreProperties["aliasRelease"].toString()
            keyPassword = keystoreProperties["passwordRelease"].toString()
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false

            versionNameSuffix = "-debug"
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName(BuildType.DEBUG.name)
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            signingConfig = signingConfigs.getByName(BuildType.RELEASE.name)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Internal dependencies
    implementation(project(":kodex"))

    // AndroidX
    implementation(libs.androidx.activity.compose)

    // Kotlin Serialization
    implementation(libs.kotlin.serialization)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.core)
    implementation(libs.compose.navigation)
    implementation(libs.compose.icons.extended)
    debugImplementation(libs.compose.ui.tooling)
}
