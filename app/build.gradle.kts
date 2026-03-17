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
        versionCode = 7
        versionName = "1.7.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}
