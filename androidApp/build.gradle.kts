plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

val appIdSuffix = providers.gradleProperty("propodcast.applicationIdSuffix").getOrElse("")

android {
    namespace = "com.propodcast.app.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.propodcast.app$appIdSuffix"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        debug { applicationIdSuffix = ".debug" }
        release { isMinifyEnabled = false }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.androidx.activity.compose)
}
