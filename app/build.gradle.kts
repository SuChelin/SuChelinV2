import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id("com.google.firebase.crashlytics")
}

fun getApiKey(propertyKey: String): String {
    return gradleLocalProperties(rootDir).getProperty(propertyKey)
}

android {
    namespace = "com.suchelin.android"
    compileSdk = 34

    defaultConfig {
        manifestPlaceholders += mapOf()
        applicationId = "com.Guide.suchelin"
        minSdk = 28
        targetSdk = 34
        versionCode = 12
        versionName = "2.1.0"

        buildConfigField("String", "NAVER_CLIENT_ID", "\"${getApiKey("NAVER_CLIENT_ID")}\"")
        manifestPlaceholders["NAVER_CLIENT_ID"] = getApiKey("NAVER_CLIENT_ID")

        buildConfigField("String", "ADMOB_ID", "\"${getApiKey("ADMOB_ID")}\"")
        manifestPlaceholders["ADMOB_ID"] = getApiKey("ADMOB_ID")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            android.buildFeatures.buildConfig = true
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0"
    }

    buildFeatures {
        dataBinding = true
        compose = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(Coil.Coil)
    implementation(AndroidX.CORE_KTX)
    implementation(AndroidX.APP_COMPAT)
    implementation(Google.MATERIAL)
    implementation(AndroidX.FRAGMENT_KTX)
    implementation(AndroidX.LIFECYCLE_VIEWMODEL_KTX)
    implementation(AndroidX.MATERIAL3_THEME)
    implementation(Firebase.FirebasFireStore)
    implementation(Firebase.FirebaseAuth)
    implementation(Firebase.FirebaseCrash)
    testImplementation(AndroidX.JUNIT)
    androidTestImplementation(AndroidX.EXT_JUNIT)
    androidTestImplementation(AndroidX.ESPRESSO_CORE)
    implementation(AndroidX.NAVIGATION_FRAGMENT_KTX)
    implementation(AndroidX.NAVIGATION_UI_KTX)
    implementation(AndroidX.PREFERENCES_DATASTORE)
    implementation(KotlinX.KOTLINX_COROUTINE)
    implementation(Jakewharton.TIMBER)
    implementation(AndroidX.SPLASH_SCREEN)
    implementation(Glide.GLIDE)
    implementation(AndroidX.NAVIGATION_COMPOSE)
    implementation(platform(Firebase.FirebaseBom))
    implementation(Firebase.FirebaseAnalytics)
    implementation(Firebase.FirebaseRTDB)
    implementation(Naver.NaverMap)
    implementation(Airbnb.Lottie)
    implementation(Admob.Admob)
    implementation(Compose.UI)
    implementation(Compose.Material)
    implementation(Compose.Foundation)
    implementation(Jsoup.Jsoup)
    implementation(AndroidX.ROOM)
    implementation(AndroidX.ROOM_COROUTINE)
    annotationProcessor(AndroidX.ROOM_PROCESSOR)
    ksp(AndroidX.ROOM_COMPILER)
}