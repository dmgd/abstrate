dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "abstrate"

file("libraries")
    .listFiles()
    .orEmpty()
    .forEach {
        if (it.isDirectory) {
            include(":libraries:${it.name}")
        }
    }