plugins {
    alias(libs.plugins.agp.app)
}

val releaseKeystore = providers.gradleProperty("releaseKeystore")
val releaseKeystorePassword = providers.gradleProperty("releaseKeystorePassword")
val releaseKeyAlias = providers.gradleProperty("releaseKeyAlias")
val releaseKeyPassword = providers.gradleProperty("releaseKeyPassword")
val hasReleaseSigning = releaseKeystore.isPresent
    && releaseKeystorePassword.isPresent
    && releaseKeyAlias.isPresent
    && releaseKeyPassword.isPresent

android {
    namespace = "com.saltsplit.ks"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.saltsplit.ks"
        minSdk = 23
        targetSdk = 36
        versionCode = providers.gradleProperty("versionCode")
            .orNull
            ?.toIntOrNull()
            ?: 1
        versionName = providers.gradleProperty("versionName").orNull ?: "0.0.1"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(releaseKeystore.get())
                storePassword = releaseKeystorePassword.get()
                keyAlias = releaseKeyAlias.get()
                keyPassword = releaseKeyPassword.get()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (hasReleaseSigning) {
                signingConfigs["release"]
            } else {
                signingConfigs["debug"]
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    compileOnly(libs.legacy.xposed.api)
    testImplementation(libs.junit4)
}
