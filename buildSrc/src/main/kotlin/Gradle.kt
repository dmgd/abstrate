import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.accessors.dm.LibrariesForLibsInPluginsBlock
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.PluginDependenciesSpecScope
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.the

internal val Project.libs
    get() =
        rootProject.the<LibrariesForLibs>()
