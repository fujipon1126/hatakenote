plugins {
    alias(libs.plugins.hatakenote.android.library)
    alias(libs.plugins.hatakenote.android.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.hatakenote.core.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.android)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
}
