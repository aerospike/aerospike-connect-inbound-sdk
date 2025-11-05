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
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate

/**
 * Setup Java tasks and compiler arguments.
 */
fun Project.setupJavaBuild() {
    val compileJava: JavaCompile by tasks
    compileJava.sourceCompatibility = "11"
    compileJava.targetCompatibility = "11"
    compileJava.options.apply {
        compilerArgs.add("-Xlint:all")
        compilerArgs.add("-Werror")
        compilerArgs.add("-Xlint:-processing")
    }

    val java = (project.extensions["java"] as JavaPluginExtension)
    java.apply {
        withJavadocJar()
        withSourcesJar()
    }

    tasks.getByName("javadoc", Javadoc::class) {
        options {
            this as StandardJavadocDocletOptions

            // Fail on Javadoc lint errors.
            addBooleanOption("Xdoclint:all", true)
            // This is a hack as we are not using Java15+.
            // See https://stackoverflow.com/a/49544352/5611068.
            addStringOption("Xwerror", "-quiet")
        }
    }
}
