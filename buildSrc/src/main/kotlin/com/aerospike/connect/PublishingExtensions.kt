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

package com.aerospike.connect

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

/**
 * Setup the Maven publication.
 *
 * No repository is registered. The release workflow only generates the POM and
 * Gradle Module Metadata from this publication; signing, deployment, and Maven
 * Central publication are owned by aerospike/shared-workflows.
 */
fun Project.setupPublishingTasks() {
    val publishing =
        (project.extensions["publishing"] as PublishingExtension)

    publishing.publications {
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
                description.set(
                    "Inbound connector SDK for change notification transformers."
                )
                url.set(
                    "https://github.com/aerospike/aerospike-connect-inbound-sdk"
                )
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set(
                            "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        )
                    }
                }
                scm {
                    connection.set(
                        "scm:git@github.com:aerospike/aerospike-connect-inbound-sdk.git"
                    )
                    developerConnection.set(
                        "scm:git@github.com:aerospike/aerospike-connect-inbound-sdk.git"
                    )
                    url.set(
                        "https://github.com/aerospike/aerospike-connect-inbound-sdk"
                    )
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

    embedMavenDescriptor()
}

/**
 * Adds `META-INF/maven/<groupId>/<artifactId>/pom.properties` to the main,
 * sources, and javadoc jars, the way the Maven archiver does.
 *
 * The release pipeline derives the Maven coordinates of every jar it uploads.
 * A `-sources` or `-javadoc` jar has no sibling POM to read, so without the
 * embedded descriptor its group id is unknown and it is stored outside the
 * coordinates of the main artifact instead of as a classifier of it.
 */
private fun Project.embedMavenDescriptor() {
    val groupId = project.group.toString()
    val artifactId = project.name

    val pomProperties = tasks.register("mavenDescriptorProperties") {
        val output = layout.buildDirectory
            .file("maven-descriptor/pom.properties")
        val versionProvider = provider { project.version.toString() }

        outputs.file(output)
        inputs.property("groupId", groupId)
        inputs.property("artifactId", artifactId)
        inputs.property("version", versionProvider)

        doLast {
            val file = output.get().asFile
            file.parentFile.mkdirs()
            file.writeText(
                """
                groupId=$groupId
                artifactId=$artifactId
                version=${versionProvider.get()}
                """.trimIndent() + "\n"
            )
        }
    }

    listOf("jar", "sourcesJar", "javadocJar").forEach { name ->
        tasks.named(name, Jar::class.java) {
            from(pomProperties) {
                into("META-INF/maven/$groupId/$artifactId")
            }
        }
    }
}
