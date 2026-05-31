plugins {
    alias(libs.plugins.hatakenote.android.library)
    alias(libs.plugins.hatakenote.android.hilt)
}

android {
    namespace = "com.example.hatakenote.core.firestore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Firebase Firestore
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
}
