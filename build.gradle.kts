/*
 *
 *  Copyright 2012-2021 Aerospike, Inc.
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
        jcenter()
    }
    dependencies {
        classpath("net.researchgate:gradle-release:2.6.0")
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
            toolVersion = "0.8.5"
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
        jcenter()
    }

    group = "com.aerospike"

    // Common dependency versions.
    extra["aerospikeClientVersion"] = "6.1.4"
    extra["jacksonVersion"] = "2.13.4"

    dependencies {
        // Lombok for its @Generated annotation that jacoco ignores
        "compileOnly"("org.projectlombok:lombok:1.18.12")
        "annotationProcessor"("org.projectlombok:lombok:1.18.12")

        // JSR 305 for annotations
        "api"("com.google.code.findbugs:jsr305:3.0.2")

        // Aerospike Java Client
        "api"("com.aerospike:aerospike-client:${project.extra["aerospikeClientVersion"]}")

        // Jackson annotation
        "api"("com.fasterxml.jackson.core:jackson-annotations:${project.extra["jacksonVersion"]}")
    }

    val compileJava: JavaCompile by tasks
    compileJava.sourceCompatibility = "1.8"
    compileJava.targetCompatibility = "1.8"
    compileJava.options.apply {
        compilerArgs.add("-Xlint:all")
        compilerArgs.add("-Werror")
        compilerArgs.add("-Xlint:-processing")
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
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    scm {
                        connection.set("scm:git@github.com:aerospike/aerospike-connect-inbound-sdk.git")
                        developerConnection.set("scm:git@github.com:aerospike/aerospike-connect-inbound-sdk.git")
                        url.set("https://github.com/aerospike/aerospike-connect-inbound-sdk")
                    }
                    developers{
                        developer {
                            name.set("Aerospike")
                            email.set("developers@aerospike.com")
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

    @Suppress("UnstableApiUsage")
    java {
        withJavadocJar()
        withSourcesJar()
    }

    val snykTokens: String by project
    val snykToken = snykTokens.split(",").map { it.trim() }.random()

    tasks.create<Exec>("setup-snyk") {
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
