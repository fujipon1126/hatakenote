plugins {
    alias(libs.plugins.hatakenote.jvm.library)
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.javax.inject)
}
