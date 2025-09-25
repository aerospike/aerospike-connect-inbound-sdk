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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.breadmoirai.githubreleaseplugin.GithubApi
import org.slf4j.LoggerFactory
import java.io.File
import java.util.Locale

/**
 * Configuration for a release.
 */
data class GithubReleaseConfiguration(
    val owner: String = "aerospike",
    val repo: String = "aerospike-connect-inbound-sdk",
    val tagName: String = "",
    val targetCommitish: String = "master",
    val releaseName: String = "",
    val body: String = "",
) {
    companion object {
        private var objectMapper: ObjectMapper = ObjectMapper()

        init {
            objectMapper.registerModule(KotlinModule.Builder().build())
        }

        fun fromFile(file: File): GithubReleaseConfiguration {
            return objectMapper.readValue(
                file,
                GithubReleaseConfiguration::class.java
            )
        }

        fun toFile(
            file: File,
            githubReleaseConfiguration: GithubReleaseConfiguration
        ) {
            return objectMapper.writeValue(file, githubReleaseConfiguration)
        }
    }
}

object GithubRelease {
    private val log = LoggerFactory.getLogger(GithubRelease::class.java)

    fun publishRelease(githubReleaseConfiguration: GithubReleaseConfiguration) {
        val authValue = "Token ${System.getenv("GITHUB_TOKEN")}"
        val api = GithubApi(authValue)
        createRelease(api, githubReleaseConfiguration)
    }

    private fun createRelease(
        api: GithubApi,
        githubReleaseConfiguration: GithubReleaseConfiguration
    ) {
        val values = mapOf(
            "tag_name" to githubReleaseConfiguration.tagName,
            "target_commitish" to githubReleaseConfiguration.targetCommitish,
            "name" to githubReleaseConfiguration.releaseName,
            "body" to githubReleaseConfiguration.body,
            "draft" to false,
            "prerelease" to false
        )

        val response = api.postRelease(
            githubReleaseConfiguration.owner,
            githubReleaseConfiguration.repo,
            values
        )

        if (response.code != 201) {
            if (response.code == 404) {
                throw Exception(
                    "404 Repository with Owner: '${githubReleaseConfiguration.owner}' and Name: '${githubReleaseConfiguration.repo}' was not found"
                )
            }
            throw Exception(
                "Could not create release: ${response.code} ${response.message}\n${response.body}"
            )
        } else {
            log.info("Status ${response.message.uppercase(Locale.getDefault())}")
            log.info("${response.body}")
        }
    }
}
