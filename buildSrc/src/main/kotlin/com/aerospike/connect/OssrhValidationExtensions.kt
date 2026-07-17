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

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.net.HttpURLConnection
import java.net.URI
import java.util.Base64

private const val OSSRH_CREDENTIALS_URL =
    "https://ossrh-staging-api.central.sonatype.com/manual/search/repositories?ip=any"

/**
 * Register OSSRH credential validation and wire it ahead of publish tasks so
 * auth failures fail CI snapshot builds and release before GitHub publish.
 */
fun Project.setupOssrhCredentialValidation() {
    tasks.register("verifyOssrhCredentials") {
        group = "verification"
        description =
            "Verify OSSRH credentials before Maven Central publish"

        onlyIf {
            findProperty("ossrhUsername") != null &&
                findProperty("ossrhPassword") != null
        }

        doLast {
            verifyOssrhCredentials()
        }
    }

    tasks.named("publish").configure {
        dependsOn("verifyOssrhCredentials")
    }
    tasks.named("publishGithubRelease").configure {
        dependsOn("verifyOssrhCredentials")
    }
}

private fun Project.verifyOssrhCredentials() {
    val username = findProperty("ossrhUsername") as? String
        ?: throw GradleException(
            "OSSRH username not configured (ossrhUsername / OSSRH_USERNAME)"
        )
    val password = findProperty("ossrhPassword") as? String
        ?: throw GradleException(
            "OSSRH password not configured (ossrhPassword / OSSRH_PASSWORD)"
        )

    val connection = URI(OSSRH_CREDENTIALS_URL).toURL().openConnection()
        as HttpURLConnection
    connection.requestMethod = "GET"
    // Use the same Basic auth that maven-publish sends for release uploads.
    connection.setRequestProperty(
        "Authorization",
        "Basic " + Base64.getEncoder().encodeToString(
            "$username:$password".toByteArray(Charsets.UTF_8)
        )
    )

    val responseCode = connection.responseCode
    if (responseCode != HttpURLConnection.HTTP_OK) {
        val errorBody = connection.errorStream?.bufferedReader()?.readText()
            ?: connection.inputStream?.bufferedReader()?.readText()
        throw GradleException(
            "OSSRH credential validation failed (HTTP $responseCode)" +
                (errorBody?.let { ": $it" } ?: "")
        )
    }
    logger.lifecycle("OSSRH credentials validated successfully")
}
