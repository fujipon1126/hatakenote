plugins {
    alias(libs.plugins.hatakenote.android.feature)
}

android {
    namespace = "com.example.hatakenote.feature.planting"
}

dependencies {
    implementation(libs.coil.compose)
}
