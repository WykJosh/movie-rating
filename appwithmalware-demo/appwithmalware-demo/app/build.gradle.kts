//plugins {
//    id("com.android.application")
//    kotlin("android")
//    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
//    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
//}
//
//android {
//    namespace = "com.example.movierating"
//    compileSdk = 36
//
//    defaultConfig {
//        applicationId = "com.example.movieratingtest"
//        minSdk = 24
//        targetSdk = 36
//        versionCode = 1
//        versionName = "1.0"
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildFeatures {
//        buildConfig = true
//        compose = true
//    }
//
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.6.4"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_17
//        targetCompatibility = JavaVersion.VERSION_17
//    }
//
//    kotlinOptions {
//        jvmTarget = "17"
//    }
//
////    packaging {
////
////        resources {
////            excludes += "META-INF/NOTICE.md"
////            excludes += "META-INF/LICENSE.md"
////
//////don’t include any META-INF/NOTICE.md in  final APK, btw these are the ones that come with the mail needed stuff for malware
////        }
////    }
//
//
//}
//
//dependencies {
//    // Core Android
//    implementation("androidx.core:core-ktx:1.12.0")
//    implementation("androidx.appcompat:appcompat:1.6.1")
//
//    // Jetpack Compose
//    implementation("androidx.compose.ui:ui:1.6.4")
//    implementation("androidx.compose.material3:material3:1.4.0-alpha13")
//    implementation("androidx.compose.ui:ui-tooling-preview:1.6.4")
//    implementation(libs.androidx.compose.runtime)
//    implementation(libs.androidx.compose.foundation.layout)
//    implementation(libs.firebase.crashlytics.buildtools)
//    implementation(libs.androidx.lifecycle.viewmodel.ktx)
//    implementation(libs.androidx.compiler)
//    debugImplementation("androidx.compose.ui:ui-tooling:1.6.4")
//    implementation("androidx.activity:activity-compose:1.8.0")
//
//    // Room Database
//    val roomVersion = "2.6.1"
//    implementation("androidx.room:room-runtime:$roomVersion")
//    implementation("androidx.room:room-ktx:$roomVersion")
//    ksp("androidx.room:room-compiler:2.6.1")
//
//    // Retrofit & OkHttp
//    implementation("com.squareup.retrofit2:retrofit:2.9.0")
//    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
//    implementation("com.squareup.okhttp3:okhttp:4.11.0")
//    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
//
//    // Coroutines
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
//
//    // Lifecycle & ViewModel
//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
//
//    // Image Loading
//    implementation("io.coil-kt:coil-compose:2.4.0")
//
//    // Permissions
//    // implementation("androidx.core:core:1.12.0")
//
//    // Testing (optional)
//    testImplementation("junit:junit:4.13.2")
//    androidTestImplementation("androidx.test.ext:junit:1.1.5")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//
//    implementation("androidx.compose.material:material-icons-extended:1.6.4")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
//
//    // malware ------------------
////    implementation("com.sun.mail:android-mail:1.6.8") {
////        exclude(group = "com.google.guava", module = "guava")
////        exclude(group = "com.google.guava", module = "listenablefuture")
////    }
////    implementation("com.sun.mail:android-activation:1.6.8") {
////        exclude(group = "com.google.guava", module = "guava")
////        exclude(group = "com.google.guava", module = "listenablefuture")
////    }
////    implementation("org.simplejavamail:simple-java-mail:7.7.0")
////
////    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
////    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
//    // --------------
//
//
//}



plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}
android {
    namespace = "com.example.movierating"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.movierating"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.4"
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


    packaging {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "LICENSE-2.0.txt"

//don’t include any META-INF/NOTICE.md in final APK.
        }
    }
}
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.6.4")
    implementation("androidx.compose.material3:material3:1.4.0-alpha13")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.4")
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.4")
    implementation("androidx.activity:activity-compose:1.8.0")
    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:2.6.1")
    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.4.0")
    // Permissions
    // implementation("androidx.core:core:1.12.0")
    // Testing (optional)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.4")

    // these have been added
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
//    implementation("com.sun.mail:android-mail:1.6.8")
//    implementation("com.sun.mail:android-activation:1.6.8")

    implementation("org.simplejavamail:simple-java-mail:7.7.0") {
        exclude(group = "com.github.bbottema", module = "jetbrains-runtime-annotations")
    }

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    // cuz it MovieRating app to the separate email module, so EmailSender.sendMovieRatings() works from MainActivity.
// instead of that inside movierating - jusst

    implementation("com.squareup.okhttp3:mockwebserver:4.12.0") //for running a mock web server inside of the app iteself

    implementation("com.squareup.okhttp3:okhttp-tls:4.12.0")




}
