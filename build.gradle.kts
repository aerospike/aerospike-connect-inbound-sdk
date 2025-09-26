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

import com.aerospike.connect.createGithubPublishTasks
import com.aerospike.connect.setupJavaBuild
import com.aerospike.connect.setupPublishingTasks
import com.aerospike.connect.setupReleaseTasks
import com.aerospike.connect.setupTests
import com.aerospike.connect.setupVulnerabilityScanning

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
    extra["aerospikeClientVersion"] = "9.2.0"
    extra["jacksonVersion"] = "2.18.4"

    dependencies {
        // Lombok for its @Generated annotation that jacoco ignores
        val lombokVersion = "1.18.38"
        compileOnly("org.projectlombok:lombok:$lombokVersion")
        annotationProcessor("org.projectlombok:lombok:$lombokVersion")

        // JSR 305 for annotations
        "api"("com.google.code.findbugs:jsr305:3.0.2")

        // Aerospike Java Client
        "api"("com.aerospike:aerospike-client-jdk8:${project.extra["aerospikeClientVersion"]}")

        // Jackson annotation
        "api"("com.fasterxml.jackson.core:jackson-annotations:${project.extra["jacksonVersion"]}")

        // Test dependencies
        testImplementation("com.aerospike:aerospike-client-jdk8:${project.extra["aerospikeClientVersion"]}")
        testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    project.setupJavaBuild()
    project.setupReleaseTasks()
    project.setupPublishingTasks()
    project.createGithubPublishTasks()
    project.setupVulnerabilityScanning()
    project.setupTests()
}
