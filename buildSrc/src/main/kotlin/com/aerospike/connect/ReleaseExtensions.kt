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
import org.gradle.kotlin.dsl.configure

/**
 * Setup release tasks.
 */
fun Project.setupReleaseTasks() {
    tasks.getByName("afterReleaseBuild").dependsOn("publish")

    /**
     * Setup release task properties.
     */
    project.extensions.configure(ReleaseExtension::class) {
        git.requireBranch.set("master")
        tagTemplate.set("$version")
    }
}

/**
 * Check if current project version is a snapshot version.
 */
fun Project.isSnapshotVersion(): Boolean {
    return version.toString().endsWith("-SNAPSHOT")
}

/**
 * Check if we are running a release task.
 */
fun Project.hasReleaseTask(): Boolean {
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
