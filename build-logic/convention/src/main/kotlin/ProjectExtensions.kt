import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

// For use in Convention Plugin code (.kt files in build-logic)
// Named 'catalog' to avoid conflict with Gradle's generated 'libs' accessor in build.gradle.kts
val Project.catalog
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.findVersionString(alias: String): String =
    findVersion(alias).get().toString()

fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { NoSuchElementException("Library $alias not found in version catalog") }

fun VersionCatalog.plugin(alias: String) =
    findPlugin(alias).orElseThrow { NoSuchElementException("Plugin $alias not found in version catalog") }
