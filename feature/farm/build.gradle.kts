plugins {
    alias(libs.plugins.hatakenote.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.hatakenote.feature.farm"
}

dependencies {
    implementation(project(":core:firestore"))
}
