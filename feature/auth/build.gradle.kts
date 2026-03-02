plugins {
    alias(libs.plugins.hatakenote.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.hatakenote.feature.auth"
}

dependencies {
    implementation(project(":core:auth"))

    // Google Sign-In (Credential Manager)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
}
