/*
 *
 *  Copyright 2012-2020 Aerospike, Inc.
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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.4.0"

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.0")
        classpath("net.researchgate:gradle-release:2.6.0")
    }
}

plugins {
    `lifecycle-base`
    jacoco
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
        plugin("org.jetbrains.kotlin.jvm")
        plugin("jacoco")
        plugin("maven-publish")
        plugin("net.researchgate.release")
    }

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven {
            url = uri("http://192.168.200.15:8080/repository/snapshots")
        }
        maven {
            url = uri("http://192.168.200.15:8080/repository/internal")
        }
    }

    group = "com.aerospike"

    // Common dependency versions.
    extra["kotlinVersion"] = kotlinVersion
    extra["aerospikeClientVersion"] = "4.4.18"
    extra["jacksonVersion"] = "2.11.+"

    dependencies {
        // Lombok for its @Generated annotaiton that jacoco ignores
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

    /**
     * Ensure code is compiled for java 8 target.
     */
    val compileKotlin: KotlinCompile by tasks
    compileKotlin.kotlinOptions {
        jvmTarget = "1.8"
    }
    val compileTestKotlin: KotlinCompile by tasks
    compileTestKotlin.kotlinOptions {
        jvmTarget = "1.8"
    }

    val compileJava: JavaCompile by tasks
    compileJava.targetCompatibility = "1.8"
    val compileTestJava: JavaCompile by tasks
    compileTestJava.targetCompatibility = "1.8"

    project.extensions.configure(ReleaseExtension::class) {
        tagTemplate = "$name-$version"
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

    val publishing = (project.extensions["publishing"] as PublishingExtension)

    publishing.publications {

        create<MavenPublication>("maven") {
            from(components["java"])
        }

        publishing.repositories {
            val repositoryUrl = uri(if (isSnapshotVersion(project
                            .version)) {
                "http://192.168.200.15:8080/repository/snapshots"
            } else {
                "http://192.168.200.15:8080/repository/internal"
            })
            val aerospikeRepoUser: String by project
            val aerospikeRepoPassword: String by project

            maven {
                name = "AerospikeMavenRepo"
                url = repositoryUrl
                credentials {
                    username = aerospikeRepoUser
                    password = aerospikeRepoPassword
                }
            }
        }

        tasks.withType<PublishToMavenRepository>().configureEach {
            onlyIf {
                // Upload is snap shot version.
                // If a proper release version upload only when release task is
                // present. This prevents re-releasing re builds of released
                // version. This is just sanity because our repository fails
                // re-upload of a released artifact.
                isSnapshotVersion(project.version) || hasReleaseTask()
            }
        }

        // Bring latest snapshots.
        configurations.all {
            resolutionStrategy {
                cacheChangingModulesFor(0, TimeUnit.SECONDS)
            }
        }
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
        }
    }

    return hasRelease
}
