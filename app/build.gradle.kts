plugins {
    alias(libs.plugins.agp.app)
}

android {
    namespace = "com.spasc.module"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.spasc.module"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs["debug"]
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
