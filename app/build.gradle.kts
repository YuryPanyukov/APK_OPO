plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.yury.recyclerview"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yury.recyclerview"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {

    // Kotlin & AndroidX (по main_mind.md раздел 9)
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.fragment:fragment-ktx:1.5.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    ksp("androidx.room:room-compiler:2.6.1")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Compose (план предусматривает Jetpack Compose)
    implementation("androidx.compose.ui:ui:1.6.8")
    implementation("androidx.compose.ui:ui-graphics:1.6.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.8")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.8")

    // CameraX
    val cameraxVersion = "1.3.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    // Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // QR reader (оставляем существующий)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.3.0")

    // Material & constraintlayout
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Lifecycle & ViewModel (Compose-friendly)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui-test-junit4:1.6.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")

    // Navigation (ещё один вариант)
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.7")
}