plugins {
    id("com.android.application")
}

android {
    namespace = "com.colorclock.watchface"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.colorclock.watchface"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

// No runtime dependencies – the watch face is purely declarative WFF XML.
