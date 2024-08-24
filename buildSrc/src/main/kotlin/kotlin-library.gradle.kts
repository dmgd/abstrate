import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.util.Base64

plugins {
    id("org.jetbrains.kotlin.jvm")
    `maven-publish`
    signing
    distribution
}

group = "dev.abstrate"

tasks.withType<Test> {
    useJUnitPlatform()
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR)
        exceptionFormat = TestExceptionFormat.FULL
    }
    reports {
        html.required = true
        junitXml.required = false
    }
}

kotlin.jvmToolchain(libs.findVersion("jdk").get().requiredVersion.toInt())

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.findLibrary("junit-engine").get())
}

val version = rootProject.file("version").readText().trim()
project.version = version
val isReleaseVersion = !version.endsWith("-SNAPSHOT")

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("lib") {
            from(components["java"])
            pom {
                name = project.name
                description = "Libraries for building stuff"
                url = "https://github.com/dmgd/abstrate"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        name = "Jordan Stewart"
                        email = "jordan.r.stewart@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:git@github.com:dmgd/abstrate.git"
                    developerConnection = "scm:git:git@github.com:dmgd/abstrate.git"
                    url = "https://github.com/dmgd/abstrate"
                }
            }
        }
        create<MavenPublication>("bundle") {
            artifact(tasks.distZip)
        }
    }
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("repos/bundles"))
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY"),
        System.getenv("SIGNING_PASSWORD"),
    )
    sign(publishing.publications)
}

distributions {
    main {
        contents {
            from(layout.buildDirectory.dir("repos/bundles"))
            eachFile {
                path = path.substringAfter("/")
            }
        }
    }
}

tasks.distZip {
    dependsOn("publishLibPublicationToMavenRepository")
}

val publishToMavenCentral by tasks.registering(Exec::class) {
    dependsOn(tasks.distZip)
    val credentials = System.getenv("MAVEN_CENTRAL_CREDENTIALS")
    val token = Base64.getEncoder().encodeToString(credentials.toByteArray())
    commandLine(
        "curl",
        "--request",
        "POST",
        "--header",
        "Authorization: Bearer $token",
        "--form",
        "bundle=@build/distributions/${project.name}-${project.version}.zip",
        "https://central.sonatype.com/api/v1/publisher/upload?publishingType=AUTOMATIC",
    )
}

val ciDependsOn = if (isReleaseVersion) publishToMavenCentral else tasks.distZip

tasks.register("ci") {
    dependsOn(ciDependsOn)
}
