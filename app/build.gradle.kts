import java.util.Properties

plugins {
    alias(libs.plugins.hatakenote.android.application)
    alias(libs.plugins.hatakenote.android.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.appdistribution)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

// バージョン管理
val versionPropertiesFile = rootProject.file("version.properties")
val versionProperties = Properties().apply {
    if (versionPropertiesFile.exists()) {
        load(versionPropertiesFile.inputStream())
    }
}

var appVersionCode = versionProperties.getProperty("VERSION_CODE", "1").toInt()
var appVersionName = versionProperties.getProperty("VERSION_NAME", "1.0.0")

// -PbumpVersion=true が指定された場合にバージョンをインクリメント
if (project.hasProperty("bumpVersion") && project.property("bumpVersion") == "true") {
    appVersionCode += 1
    // VERSION_NAME の patch バージョンをインクリメント (例: 1.0.0 -> 1.0.1)
    val versionParts = appVersionName.split(".")
    if (versionParts.size == 3) {
        val patch = versionParts[2].toIntOrNull() ?: 0
        appVersionName = "${versionParts[0]}.${versionParts[1]}.${patch + 1}"
    }
    // ファイルに保存
    versionProperties.setProperty("VERSION_CODE", appVersionCode.toString())
    versionProperties.setProperty("VERSION_NAME", appVersionName)
    versionPropertiesFile.outputStream().use { versionProperties.store(it, null) }
    println("Version bumped to: versionCode=$appVersionCode, versionName=$appVersionName")
}

android {
    namespace = "com.example.hatakenote"

    signingConfigs {
        create("release") {
            val storeFilePath = localProperties.getProperty("RELEASE_STORE_FILE", "")
            if (storeFilePath.isNotEmpty()) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD", "")
                keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS", "")
                keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD", "")
            }
        }
    }

    defaultConfig {
        applicationId = "com.example.hatakenote"
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\"")
        buildConfigField("String", "FIREBASE_WEB_CLIENT_ID", "\"${localProperties.getProperty("FIREBASE_WEB_CLIENT_ID", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

firebaseAppDistribution {
    groups = "testers"
    releaseNotes = "テスト版"
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))
    implementation(project(":core:auth"))
    implementation(project(":core:firestore"))

    // Feature modules
    implementation(project(":feature:home"))
    implementation(project(":feature:plot"))
    implementation(project(":feature:planting"))
    implementation(project(":feature:worklog"))
    implementation(project(":feature:crop"))
    implementation(project(":feature:calendar"))
    implementation(project(":feature:assistant"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:farm"))

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // WorkManager for Hilt
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Google Sign-In (Credential Manager)
    implementation(libs.play.services.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
