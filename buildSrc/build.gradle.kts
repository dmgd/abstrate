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
    implementation(plugin(libs.plugins.kotlin))
    implementation(plugin(libs.plugins.jib))
}

fun plugin(provider: Provider<PluginDependency>) =
    provider.get()
        .run {
            "$pluginId:$pluginId.gradle.plugin:$version"
        }
