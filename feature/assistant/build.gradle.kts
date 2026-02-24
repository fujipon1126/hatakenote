plugins {
    alias(libs.plugins.hatakenote.android.feature)
}

android {
    namespace = "com.example.hatakenote.feature.assistant"
}

dependencies {
    implementation(libs.coil.compose)
}
