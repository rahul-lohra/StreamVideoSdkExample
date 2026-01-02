plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "rahul.lohra.streamvideosdkexample"
    compileSdk = 36

    defaultConfig {
        applicationId = "rahul.lohra.streamvideosdkexample"
        minSdk = 24
        targetSdk = 36
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
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material)
    implementation(libs.androidx.navigation)
    implementation("com.auth0:java-jwt:4.5.0")
    implementation ("io.getstream:stream-android-push-firebase:1.3.2")
    implementation(libs.firebase.messaging)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val video_sdk = "1.18.3-202512241148-SNAPSHOT"
    implementation("io.getstream:stream-video-android-core:${video_sdk}")
    implementation("io.getstream:stream-video-android-ui-compose:$video_sdk")

    val chat_sdk = "6.29.0"
    implementation("io.getstream:stream-chat-android-client:$chat_sdk")
    implementation("io.getstream:stream-chat-android-state:$chat_sdk")
    implementation("io.getstream:stream-chat-android-offline:${chat_sdk}")
    implementation("io.getstream:stream-chat-android-ui-components:${chat_sdk}")
    implementation("io.getstream:stream-chat-android-compose:${chat_sdk}")
}