import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.shelfship"
    compileSdk = 35
    android.buildFeatures.buildConfig = true

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

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.credentials:credentials-play-services-auth:<latest_version>")
    implementation("androidx.credentials:credentials:<latest_version>")
    implementation("com.google.android.libraries.identity.googleid:googleid:<latest_version>")
    implementation(libs.androidx.lifecycle.ktx)
    implementation("com.google.firebase:firebase-firestore:<latest-version>")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.android.material:material")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.code.gson:gson:2.9.1")

    implementation ("androidx.activity:activity-compose:1.9.0")
    implementation ("androidx.compose.ui:ui:1.6.6")
    implementation ("androidx.compose.material:material:1.6.6")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
}

