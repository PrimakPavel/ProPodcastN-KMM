plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// Firebase (Analytics, Crashlytics) needs androidApp/google-services.json, which is not in git. It must list the
// KMM application ids (com.propodcast.app.kmm and com.propodcast.app.kmm.debug). Without it the app builds and runs
// without Firebase, as ProPodcastN does.
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
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
