import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.accessors.dm.LibrariesForLibsInPluginsBlock
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.PluginDependenciesSpecScope
import org.gradle.kotlin.dsl.getByType

internal val Project.libs
    get() =
        rootProject.extensions.getByName("libs") as LibrariesForLibs
