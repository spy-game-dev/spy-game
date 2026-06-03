plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "ru.internet.spygame"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.internet.spy_game"
        minSdk = 26
        targetSdk = 36
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
    // ↑ composeOptions { kotlinCompilerExtensionVersion } УБРАН —
    //   за это теперь отвечает kotlin-compose plugin (Kotlin 2.0+)

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        optIn.add("kotlin.RequiresOptIn")
    }
}

dependencies {
    // ─────────────────────────────────────────
    // AndroidX Core
    // ─────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // ─────────────────────────────────────────
    // Material Components (XML-темы: Theme.Material3.DayNight.NoActionBar)
    // ─────────────────────────────────────────
    implementation(libs.material)

    // ─────────────────────────────────────────
    // Jetpack Compose
    // ─────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)

    // ─────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ─────────────────────────────────────────
    // Lifecycle / ViewModel
    // ─────────────────────────────────────────
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)   // viewModel() в Composable

    // ─────────────────────────────────────────
    // Hilt — KSP вместо kapt(...)
    // ─────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)                            // ← было kapt(...)
    implementation(libs.androidx.hilt.navigation.compose)

    // ─────────────────────────────────────────
    // Room — room-ktx не нужен (слит в room-runtime с v2.7.0)
    // ─────────────────────────────────────────
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)                           // ← было kapt(...)

    // ─────────────────────────────────────────
    // DataStore
    // ─────────────────────────────────────────
    implementation(libs.androidx.datastore.preferences)

    // ─────────────────────────────────────────
    // Kotlinx
    // ─────────────────────────────────────────
    implementation(libs.kotlinx.serialization.json)            // Парсинг JSON-ассетов
    implementation(libs.kotlinx.coroutines.android)

    // ─────────────────────────────────────────
    // Splash Screen
    // ─────────────────────────────────────────
    implementation(libs.androidx.core.splashscreen)

    // ─────────────────────────────────────────
    // Тестирование
    // ─────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
