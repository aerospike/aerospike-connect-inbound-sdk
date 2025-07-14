/*
 *
 *  Copyright 2012-2025 Aerospike, Inc.
 *
 *  Portions may be licensed to Aerospike, Inc. under one or more contributor
 *  license agreements WHICH ARE COMPATIBLE WITH THE APACHE LICENSE, VERSION 2.0.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

import net.researchgate.release.ReleaseExtension
import java.net.URI

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

plugins {
    `lifecycle-base`
    jacoco
    `maven-publish`
    signing
    java

    id("io.snyk.gradle.plugin.snykplugin")
}

allprojects {
    // Configures the Jacoco tool version to be the same for all projects that have it applied.
    pluginManager.withPlugin("jacoco") {
        // If this project has the plugin applied, configure the tool version.
        jacoco {
            toolVersion = "0.8.13"
        }
    }

    apply {
        plugin(JavaPlugin::class.java)
        plugin("java-library")
        plugin("jacoco")
        plugin("maven-publish")
        plugin("net.researchgate.release")
        plugin("io.snyk.gradle.plugin.snykplugin")
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    group = "com.aerospike"

    // Common dependency versions.
    extra["aerospikeClientVersion"] = "9.0.5"
    extra["jacksonVersion"] = "2.19.1"

    dependencies {
        // Lombok for its @Generated annotation that jacoco ignores
        val lombokVersion = "1.18.38"
        "compileOnly"("org.projectlombok:lombok:$lombokVersion")
        "annotationProcessor"("org.projectlombok:lombok:$lombokVersion")

        // JSR 305 for annotations
        "compileOnly"("com.google.code.findbugs:jsr305:3.0.2")

        // Aerospike Java Client
        "compileOnly"("com.aerospike:aerospike-client-jdk8:${project.extra["aerospikeClientVersion"]}")

        // Jackson annotation
        "compileOnly"("com.fasterxml.jackson.core:jackson-annotations:${project.extra["jacksonVersion"]}")
    }

    val compileJava: JavaCompile by tasks
    compileJava.sourceCompatibility = "1.8"
    compileJava.targetCompatibility = "1.8"
    compileJava.options.apply {
        compilerArgs.add("-Xlint:all")
        compilerArgs.add("-Werror")
        compilerArgs.add("-Xlint:-processing")
        compilerArgs.add("-Xlint:-options")
    }

    project.extensions.configure(ReleaseExtension::class) {
        tagTemplate = "\$version"
    }

    tasks.getByName("afterReleaseBuild").dependsOn("publish")

    publishing {
        repositories {
            maven {
                val releaseRepo = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotRepo = URI("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (!isSnapshotVersion(project.version)) releaseRepo else snapshotRepo
                credentials {
                    username = project.properties["ossrhUsername"] as String
                    password = project.properties["ossrhPassword"] as String
                }
            }
        }

        publications {
            create<MavenPublication>("mavenJava") {
                artifactId = project.name
                from(components["java"])
                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }
                pom {
                    name.set("Aerospike Connect Inbound SDK")
                    description.set("Inbound SDK for message transformer or other plugins.")
                    url.set("https://github.com/aerospike/aerospike-connect-inbound-sdk")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    scm {
                        connection.set("scm:git@github.com:aerospike/aerospike-connect-inbound-sdk.git")
                        developerConnection.set("scm:git@github.com:aerospike/aerospike-connect-inbound-sdk.git")
                        url.set("https://github.com/aerospike/aerospike-connect-inbound-sdk")
                    }
                    developers {
                        developer {
                            name.set("Aerospike")
                            email.set("helpdesk@aerospike.com")
                            organization.set("Aerospike")
                            url.set("https://www.aerospike.com/")
                        }
                    }
                }
            }
        }

        tasks.withType<PublishToMavenRepository>().configureEach {
            onlyIf {
                // Upload if snap shot version.
                // If a proper release version upload only when release task is
                // present. This prevents re-releasing re builds of released
                // version.
                isSnapshotVersion(project.version) || hasReleaseTask()
            }
        }
    }

    signing {
        sign(publishing.publications.getByName("mavenJava"))
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    tasks.javadoc {
        options {
            this as StandardJavadocDocletOptions

            // Fail on Javadoc lint errors.
            addBooleanOption("Xdoclint:all", true)
            // This is a hack as we are not using Java15+.
            // See https://stackoverflow.com/a/49544352/5611068.
            addStringOption("Xwerror", "-quiet")
        }
    }

    val snykTokens: String by project
    val snykToken = snykTokens.split(",").map { it.trim() }.random()

    tasks.register("setup-snyk", Exec::class.java) {
        commandLine("${project.rootDir}/snyk", "auth", snykToken)
    }
    tasks.getByName("snyk-check-binary").finalizedBy("setup-snyk")

    /**
     * Vulnerability scanning with Snyk.
     */
    configure<io.snyk.gradle.plugin.SnykExtension> {
        setApi(snykToken)
        setSeverity("high")
        setAutoDownload(true)
        setArguments("--sub-project=" + project.name)
    }
}

/**
 * Check if current project version is a snapshot version.
 */
fun isSnapshotVersion(version: Any): Boolean {
    return version.toString().endsWith("-SNAPSHOT")
}

/**
 * Check if we are running a release task.
 */
fun hasReleaseTask(): Boolean {
    val releaseTaskName = "afterReleaseBuild"
    var hasRelease = false
    gradle.taskGraph.allTasks.forEach {
        if (it.name == releaseTaskName) {
            hasRelease = true
            return@forEach
        }
    }
    return hasRelease
}
