plugins {
    alias(libs.plugins.hatakenote.android.library)
    alias(libs.plugins.hatakenote.android.hilt)
    alias(libs.plugins.hatakenote.android.room)
}

android {
    namespace = "com.example.hatakenote.core.database"
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.android)
}
