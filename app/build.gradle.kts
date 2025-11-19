plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.exam"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.exam"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }


    buildFeatures {
        viewBinding = true
    }

    buildFeatures {
        viewBinding  = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ------- ROOM DATABASE -------
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ------- WORKMANAGER -------
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ------- CHARTS -------
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // ------- TESTING -------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
