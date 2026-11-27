import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    compilerOptions {
        // TopAppBar and friends are stable in androidx Material 3 1.5 (ProPodcastN), still experimental in
        // JetBrains Material 3 1.9.
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
    }

    android {
        namespace = "com.propodcast.app.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources { enable = true }
        // JVM unit tests for the Android target (commonTest runs there too).
        withHostTest { isIncludeAndroidResources = true }
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.backhandler)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.paging.compose)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.html.converter)
            implementation(libs.okio)
            implementation(libs.kermit)
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel.savedstate)
            implementation(libs.compose.material3.adaptive)
            implementation(libs.compose.material3.adaptive.navigation.suite)
            implementation(libs.androidx.window.core)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            api(libs.koin.android)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.session)
            implementation(libs.kotlinx.coroutines.guava)
            implementation(libs.androidx.work.runtime)
            implementation(libs.okhttp)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
            implementation(libs.androidx.glance.appwidget)
            implementation(libs.androidx.glance.material3)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.androidx.paging.testing)
            implementation(libs.turbine)
            implementation(libs.koin.test)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.robolectric)
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.work.testing)
            implementation(libs.androidx.glance.appwidget.testing)
        }
    }
}

dependencies {
    // Room code generation for every target.
    listOf("kspAndroid", "kspIosArm64", "kspIosSimulatorArm64").forEach { add(it, libs.androidx.room.compiler) }
}

room {
    schemaDirectory("$projectDir/schemas")
}

compose.resources {
    packageOfResClass = "com.propodcast.app.resources"
}

// Listen Notes settings live in local.properties (not in git):
//   LISTEN_NOTES_API_KEY=...              key for the production API
//   LISTEN_NOTES_USE_PRODUCTION=true      optional: use the production API instead of the mock server
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val listenNotesApiKey: String = localProperties.getProperty("LISTEN_NOTES_API_KEY", "").trim()
val useProductionApi: Boolean =
    localProperties.getProperty("LISTEN_NOTES_USE_PRODUCTION", "false").trim().toBoolean()
if (useProductionApi && listenNotesApiKey.isEmpty()) {
    throw GradleException("LISTEN_NOTES_USE_PRODUCTION=true needs LISTEN_NOTES_API_KEY in local.properties")
}

buildkonfig {
    packageName = "com.propodcast.app"
    objectName = "BuildKonfig"
    defaultConfigs {
        // Mock server by default (fake data, no key); production API when LISTEN_NOTES_USE_PRODUCTION=true.
        buildConfigField(
            STRING,
            "LISTEN_NOTES_BASE_URL",
            if (useProductionApi) "https://listen-api.listennotes.com/api/v2/"
            else "https://listen-api-test.listennotes.com/api/v2/",
        )
        buildConfigField(STRING, "LISTEN_NOTES_API_KEY", if (useProductionApi) listenNotesApiKey else "")
        buildConfigField(BOOLEAN, "HTTP_LOGGING", "true")
    }
}
