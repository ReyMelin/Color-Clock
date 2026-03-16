plugins {
    id("com.android.application")
}

android {
    namespace = "com.colorclock.watchface"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.colorclock.watchface"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }
    }

    sourceSets {
        getByName("main") {
            java.setSrcDirs(emptyList<String>())
            kotlin.setSrcDirs(emptyList<String>())
        }
    }
}
