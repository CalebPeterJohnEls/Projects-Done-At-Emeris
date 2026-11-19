plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.funinactionproto"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.funinactionproto"
        minSdk = 25
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // MediaPipe dependency
    implementation("com.google.mediapipe:tasks-vision:0.10.21")
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.material)
    implementation(libs.androidx.viewpager2)
    implementation("com.google.android.material:material:1.12.0")

    // Camera dependencies
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")

    // Networking — talks to FunInActionApi
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation(libs.play.services.basement)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-analytics")

    // Add the Firebase Authentication dependency (version managed by the BoM)
    implementation("com.google.firebase:firebase-auth")
    // Declare the dependency for Cloud Firestore (no version needed when using BoM)
    implementation("com.google.firebase:firebase-firestore")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}