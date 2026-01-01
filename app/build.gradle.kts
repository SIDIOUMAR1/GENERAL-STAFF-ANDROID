plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id ("com.google.firebase.crashlytics") // Add this

}

android {
    namespace = "com.genralstaff"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.genralstaff"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.2"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    bundle {
        language {
            enableSplit = false
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true   // ✅ This line ensures BuildConfig.java is generated

    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Utility
    implementation("com.hbb20:ccp:2.6.1")
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    implementation("com.karumi:dexter:6.2.2")
    implementation("de.hdodenhof:circleimageview:3.1.0")
//    implementation("io.github.chaosleung:pinview:1.4.4")
    //otp bg
    implementation("com.github.aabhasr1:OtpView:v1.1.2-ktx")

    /*uc image cropping */
//    implementation ("com.github.yalantis:ucrop:2.2.7")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.13.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.2")

    implementation("com.makeramen:roundedimageview:2.3.0")
    // view indicator
    implementation("com.github.zhpanvip:viewpagerindicator:1.2.1")
    //    //coroutine //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.4.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")
    implementation("com.airbnb.android:lottie:5.0.3")
    // view model
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:")
    //ktx
    implementation("androidx.activity:activity-ktx:1.2.3")
    implementation("androidx.fragment:fragment-ktx:1.3.4")

    //loader like ios]=
//    implementation("com.kaopiz:kprogresshud:1.2.0")

    implementation("com.google.android.libraries.places:places:2.6.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.0.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    // socket dependency
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }

//    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging:23.0.8")
    implementation("com.google.android.gms:play-services-auth:20.3.0")
//    implementation("com.google.firebase:firebase-analytics:21.5.0")
    implementation("com.github.rygelouv:android-audio-sensei:0.1.2")
    //maps directions api
    implementation("com.akexorcist:google-direction-library:1.2.1")
        implementation ("com.google.firebase:firebase-crashlytics:18.6.2")
        implementation ("com.google.firebase:firebase-analytics:21.6.1")


}