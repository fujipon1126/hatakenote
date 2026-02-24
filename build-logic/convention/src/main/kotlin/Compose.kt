import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        buildFeatures {
            compose = true
        }

        dependencies {
            val bom = catalog.library("androidx-compose-bom")
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))

            add("implementation", catalog.library("androidx-compose-ui"))
            add("implementation", catalog.library("androidx-compose-ui-graphics"))
            add("implementation", catalog.library("androidx-compose-ui-tooling-preview"))
            add("implementation", catalog.library("androidx-compose-material3"))
            add("implementation", catalog.library("androidx-compose-material-icons-extended"))
            add("debugImplementation", catalog.library("androidx-compose-ui-tooling"))
            add("debugImplementation", catalog.library("androidx-compose-ui-test-manifest"))
        }
    }
}
