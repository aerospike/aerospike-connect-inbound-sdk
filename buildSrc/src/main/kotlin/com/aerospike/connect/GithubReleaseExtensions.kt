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

import net.researchgate.release.ReleaseExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import java.io.File
import java.util.Locale

fun Project.createGithubPublishTasks() {
    val githubReleaseConfigurationFile =
        File(
            project.layout.buildDirectory.asFile.get(),
            "githubReleaseConfig.json"
        )

    /**
     * Create the list of all assets to be uploaded to GitHub after builds but
     * before the project version is incremented.
     */
    tasks.register("prepareGithubRelease", Task::class.java) {
        dependsOn("publish")
        doLast {
            val releaseVersion = project.property("release.releaseVersion")
            val releaseName = "${
                project.name.split("-").joinToString(" ") {
                    it.replaceFirstChar {
                        @Suppress("NestedLambdaShadowedImplicitParameter")
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }
                }
            } $releaseVersion"

            val body = if (project.hasProperty("releaseNotesFile")) {
                File(project.property("releaseNotesFile").toString()).readText()
            } else {
                ""
            }

            GithubReleaseConfiguration.toFile(
                githubReleaseConfigurationFile,
                GithubReleaseConfiguration(
                    tagName = "$releaseVersion",
                    releaseName = releaseName,
                    body = body
                )
            )
        }
    }

    /**
     * Publish GitHub release.
     */
    tasks.register("publishGithubRelease", Task::class.java) {
        // Ensure all assets are ready before publish.
        dependsOn("release")

        doLast {
            GithubRelease.publishRelease(
                GithubReleaseConfiguration.fromFile(
                    githubReleaseConfigurationFile
                )
            )
        }
    }

    extensions.configure(ReleaseExtension::class) {
        tagTemplate.set("$version")
    }

    // Publish and prepare release artifacts before tagging.
    tasks.getByName("afterReleaseBuild")
        .dependsOn("publish", "prepareGithubRelease")
    if (project.hasReleaseTask()) {
        tasks.withType<Test> {
            outputs.upToDateWhen { false }
        }
        // Publish and prepare release artifacts before tagging.
        listOf("publishGithubRelease", "publish").forEach {
            tasks.getByName(it).dependsOn("test")
        }
    }
}
