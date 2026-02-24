plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")

}

android {
    namespace = "com.example.myfilimapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.myfilimapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        buildTypes {
            getByName("debug") {
                isMinifyEnabled = false
                isDebuggable = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                buildConfigField(
                    "String",
                    "BASE_URL",
                    "\"${project.property("DEV_BASE_URL")}\""
                )
            }

            getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                isDebuggable = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                buildConfigField(
                    "String",
                    "BASE_URL",
                    "\"${project.property("UAT_BASE_URL")}\""
                )
            }

        }

        flavorDimensions += "env"

        productFlavors {
            create("dev") {
                dimension = "env"
                buildConfigField(
                    "String",
                    "BASE_URL",
                    "\"${project.property("DEV_BASE_URL")}\""
                )
            }
            create("uat") {
                dimension = "env"
                buildConfigField(
                    "String",
                    "BASE_URL",
                    "\"${project.property("UAT_BASE_URL")}\""
                )
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //Dagger-Hilt
    implementation("com.google.dagger:hilt-android:2.59.1")
    ksp("com.google.dagger:hilt-compiler:2.59.1")
    //compose specific library for navigation & state-management
    implementation ("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
    //splash
    implementation("androidx.core:core-splashscreen:1.0.1")
    // To use constraintlayout in compose
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")
    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    //compose specific library for navigation & state-management
    implementation ("androidx.navigation:navigation-compose:2.9.1")
    implementation ("androidx.compose.material:material:1.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    //Paging
    implementation("androidx.paging:paging-compose:3.4.0-alpha04")
    implementation("androidx.paging:paging-runtime:3.3.6")
    //image coil
    implementation("io.coil-kt:coil-compose:2.4.0")
    //Datastore
    implementation("androidx.datastore:datastore-preferences:1.1.3")
    implementation("androidx.datastore:datastore-preferences-core:1.1.3")

    implementation("androidx.media:media:1.7.0")
    implementation("com.karumi:dexter:6.2.3")

    //room
//    implementation("androidx.room:room-runtime:2.7.1")
//    implementation("androidx.room:room-ktx:2.7.1")
//    ksp("androidx.room:room-compiler:2.7.1")

}