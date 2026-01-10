plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.harshRajpurohit.musicPlayer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.harshRajpurohit.musicPlayer"
        minSdk = 23
        targetSdk = 34
        versionCode = 11
        versionName = "2.0.1"

        // For showing build version name
        buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        // For viewBinding
        viewBinding = true

        // For showing build version name
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Pull to Refresh
    implementation(libs.legacy.support)

    // Glide for image loading
    implementation(libs.glide)

    // For storing objects in shared preferences
    implementation(libs.gson)

    // Notification
    implementation(libs.androidx.media)

    // Vertical Seekbar
    implementation(libs.verticalseekbar)
    implementation(libs.androidx.media3.ui)


    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Lifecycle Scope
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

    // ExoPlayer (Media3)
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")


}