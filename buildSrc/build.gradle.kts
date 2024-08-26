plugins {
    alias(libs.plugins.kotlin)
    `kotlin-dsl`
}

kotlin.jvmToolchain(libs.versions.jdk.get().toInt())

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // add `org.gradle.accessors.dm.LibrariesForLibs` to the classpath so the version catalog works nicely inside the precompiled script plugins
    implementation(files(libs.javaClass.protectionDomain.codeSource.location))
    implementation(plugin(libs.plugins.kotlin))
    implementation(plugin(libs.plugins.jib))
}

fun plugin(provider: Provider<PluginDependency>) =
    provider.map {
        "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
    }
