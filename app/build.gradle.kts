plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "ru.molluk.git_authorization"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.molluk.git_authorization"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas".toString()
                )
            }
        }
    }

    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "git_auth.apk"
        }
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
        freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all"
    }
    buildFeatures {
        viewBinding = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

kapt {
    correctErrorTypes = true
}

dependencies {
    val javapoet = "1.13.0"
    val coreKtx = "1.13.1"
    val kotlin = "2.0.0"
    val junit = "4.13.2"
    val junitVersion = "1.2.1"
    val espressoCore = "3.6.1"
    val lifecycleRuntimeKtx = "2.9.1"
    val appcompat = "1.6.1"
    val coroutines = "1.8.0"
    val metadata = "0.9.0"
    val lifecycleVersion = "2.9.1"
    val retrofit = "2.9.0"
    val okhttp = "4.12.0"
    val room = "2.6.1"
    val navVersion = "2.9.0"
    val material = "1.9.0"
    val hilt = "2.49"
    val hiltNavigation = "1.2.0"
    val constraintlayout = "2.2.1"
    val glide = "4.16.0"
    val encryption = "1.18.0"
    val crypto = "1.1.0-beta01"

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin")
    implementation("com.squareup:javapoet:$javapoet")
    implementation("androidx.core:core-ktx:$coreKtx")
    androidTestImplementation("junit:junit:$junit")
    androidTestImplementation("androidx.test.ext:junit:$junitVersion")
    implementation("androidx.test.espresso:espresso-core:$espressoCore")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleRuntimeKtx")
    implementation("androidx.appcompat:appcompat:$appcompat")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:$metadata")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:$retrofit")
    implementation("com.squareup.retrofit2:converter-gson:$retrofit")
    implementation("com.squareup.retrofit2:converter-scalars:$retrofit")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:$okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttp")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    // Material Design
    implementation("com.google.android.material:material:$material")
    implementation("androidx.constraintlayout:constraintlayout:$constraintlayout")

    // Hilt
    implementation("com.google.dagger:hilt-android:$hilt")
    kapt("com.google.dagger:hilt-android-compiler:$hilt")
    kapt("androidx.hilt:hilt-compiler:$hiltNavigation")

    // Для работы ViewModel с Hilt
    implementation("androidx.hilt:hilt-navigation-fragment:$hiltNavigation")

    // Room
    implementation("androidx.room:room-runtime:$room")
    kapt("androidx.room:room-compiler:$room")
    implementation("androidx.room:room-ktx:$room")

    //Glide
    implementation("com.github.bumptech.glide:glide:$glide")

    //Encryption
    implementation("com.google.crypto.tink:tink-android:$encryption")

    //Crypto
    implementation("androidx.security:security-crypto:$crypto")
}