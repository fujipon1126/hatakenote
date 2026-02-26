import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("hatakenote.android.library")
                apply("hatakenote.android.library.compose")
                apply("hatakenote.android.hilt")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            dependencies {
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:data"))

                add("implementation", catalog.library("androidx-hilt-navigation-compose"))
                add("implementation", catalog.library("androidx-lifecycle-runtime-compose"))
                add("implementation", catalog.library("androidx-lifecycle-viewmodel-compose"))
                add("implementation", catalog.library("androidx-navigation-compose"))
                add("implementation", catalog.library("kotlinx-coroutines-android"))
                add("implementation", catalog.library("kotlinx-serialization-json"))
                add("implementation", catalog.library("kotlinx-datetime"))
            }
        }
    }
}
