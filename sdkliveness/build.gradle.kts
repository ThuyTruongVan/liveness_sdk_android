plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.liveness.sdk.core"
    compileSdk = 33

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
//    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.mlkit:face-detection:16.1.6")
    implementation("com.otaliastudios:cameraview:2.7.2")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.65")
    implementation("com.nimbusds:nimbus-jose-jwt:9.31")
    implementation("commons-codec:commons-codec:1.10")
//    implementation("com.google.firebase:firebase-ml-vision-face-model:20.0.2")

}