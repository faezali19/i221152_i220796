plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("com.google.gms.google-services") }
android {
 namespace="com.kinnect.i221152_i220796"
 compileSdk=36
 defaultConfig { applicationId="com.kinnect.i221152_i220796"; minSdk=24; targetSdk=34; versionCode=2; versionName="2.0"; testInstrumentationRunner="androidx.test.runner.AndroidJUnitRunner" }
 compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget="17" }
}
dependencies {
 implementation("androidx.camera:camera-core:1.4.2")
 implementation("androidx.camera:camera-camera2:1.4.2")
 implementation("androidx.camera:camera-lifecycle:1.4.2")
 implementation("androidx.camera:camera-view:1.4.2")
 implementation("androidx.camera:camera-video:1.4.2")

 implementation("androidx.core:core-ktx:1.16.0")
 implementation("androidx.appcompat:appcompat:1.7.0")
 implementation("androidx.activity:activity-ktx:1.10.1")
 implementation("androidx.constraintlayout:constraintlayout:2.2.1")
 implementation("com.google.android.material:material:1.12.0")
 implementation("androidx.recyclerview:recyclerview:1.4.0")
 implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
 implementation("com.google.firebase:firebase-auth")
 implementation("com.google.firebase:firebase-database")
 implementation("com.google.firebase:firebase-storage")
 implementation("com.google.firebase:firebase-functions")
 implementation("com.google.firebase:firebase-messaging")
 implementation("com.github.bumptech.glide:glide:4.16.0")
 implementation("io.agora.rtc:full-sdk:4.5.2")
 androidTestImplementation("androidx.test.ext:junit:1.2.1")
 androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
 androidTestImplementation("androidx.test:runner:1.6.2")
 androidTestImplementation("androidx.test:rules:1.6.1")
}
