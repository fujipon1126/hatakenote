import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import java.util.Properties

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 35

                buildFeatures {
                    buildConfig = true
                }

                // Load Gemini API key from local.properties
                val localProperties = Properties()
                val localPropertiesFile = rootProject.file("local.properties")
                if (localPropertiesFile.exists()) {
                    localProperties.load(localPropertiesFile.inputStream())
                }

                defaultConfig {
                    val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
                    buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
                }
            }

            configureAndroidCompose(extensions.getByType(ApplicationExtension::class.java))

            dependencies {
                add("implementation", catalog.library("androidx-core-ktx"))
                add("implementation", catalog.library("androidx-lifecycle-runtime-ktx"))
                add("implementation", catalog.library("androidx-activity-compose"))
            }
        }
    }
}
