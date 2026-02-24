plugins {
    alias(libs.plugins.hatakenote.android.library)
    alias(libs.plugins.hatakenote.android.library.compose)
}

android {
    namespace = "com.example.hatakenote.core.ui"
}

dependencies {
    implementation(libs.coil.compose)
}
