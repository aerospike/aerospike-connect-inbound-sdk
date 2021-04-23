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
    }

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }

    group = "com.aerospike"

    // Common dependency versions.
    extra["aerospikeClientVersion"] = "5.0.0"
    extra["jacksonVersion"] = "2.11.+"

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

        // Common test dependencies.
        "testImplementation"(
            "org.junit.jupiter:junit-jupiter-api:5.4.2"
        )
        "testImplementation"(
            "org.junit.jupiter:junit-jupiter-params:5.4.2"
        )
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    }

    val compileJava: JavaCompile by tasks
    compileJava.targetCompatibility = "1.8"
    compileJava.options.apply {
        compilerArgs.add("-Xlint:all")
        compilerArgs.add("-Werror")
        compilerArgs.add("-Xlint:-processing")
    }

    val compileTestJava: JavaCompile by tasks
    compileTestJava.targetCompatibility = "1.8"
    compileTestJava.options.apply {
        compilerArgs.add("-Xlint:all")
        compilerArgs.add("-Werror")
        compilerArgs.add("-Xlint:-processing")
    }

    project.extensions.configure(ReleaseExtension::class) {
        tagTemplate = "$version"
    }

    tasks.getByName("afterReleaseBuild").dependsOn("publish")

    /**
     * Common configuration for test tasks.
     */
    fun Test.configureTestTask() {
        val args = mutableListOf(
                "-XX:MaxPermSize=512m",
                "-Xmx4g",
                "-Xms512m",
                "-Djava.security.egd=file:/dev/./urandom",
                "-Dproject.version=${project.version}"
        )


        project.properties.forEach { (property, value) ->
            // Pass along project properties as System properties to the test.
            args += "-D$property=$value"
        }

        // Pass all project versions
        project.parent?.subprojects?.forEach {
            args += "-D${it.name.replace("\\W".toRegex(), ".")}.version=${
                it
                        .version
            }"
        }

        jvmArgs = args
    }

    /**
     * Use Junit for running tests.
     */
    tasks.getByName<Test>("test") {
        useJUnitPlatform {
            // Exclude performance tests in normal runs.
            excludeTags.add("performance")
        }

        configureTestTask()

        testLogging {
            events("passed", "skipped", "failed")
        }
    }

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
                        url.set("htps://github.com/aerospike/aerospike-connect-inbound-sdk")
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

    java {
        withJavadocJar()
        withSourcesJar()
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
