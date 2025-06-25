import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("kapt")
}

android {
    namespace = "com.example.shelfship"
    compileSdk = 35
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.shelfship"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders += mapOf("appAuthRedirectScheme" to "com.example.shelfship")

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        localProperties.load(localPropertiesFile.inputStream())
        buildConfigField(
            "String",
            "BOOKS_API_KEY",
            "\"${localProperties.getProperty("BOOKS_API_KEY", "YOUR_DEFAULT_API_KEY_IF_NOT_FOUND")}\""
        )
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Jetpack Compose bağımlılıkları
    implementation("androidx.compose.ui:ui:1.6.7")
    implementation("androidx.compose.material:material:1.6.7")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.7")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.material3:material3:1.1.0")

    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Credentials API
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation("androidx.credentials:credentials-play-services-auth:1.1.0") // örnek sürüm
    implementation("androidx.credentials:credentials:1.1.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.0.0") // örnek sürüm

    // Glide
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // Lifecycle KTX
    implementation(libs.androidx.lifecycle.ktx)

    // Test bağımlılıkları
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Material Design
    implementation("com.google.android.material:material:1.9.0")
}

