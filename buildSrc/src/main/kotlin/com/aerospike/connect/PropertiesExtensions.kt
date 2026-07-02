package com.aerospike.connect

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

fun Project.configureProperties() {
    applyCredentialsFromEnvironment()
}

/**
 * Credentials and tokens are taken from the environment when set, overriding
 * values from gradle.properties (including ~/.gradle/gradle.properties).
 */
private fun Project.applyCredentialsFromEnvironment() {
    fun setFromEnv(propertyKey: String, envVar: String) {
        val value = System.getenv(envVar) ?: return
        extra[propertyKey] = value
    }
    setFromEnv("connectSnapshotsRepoUser", "CONNECT_SNAPSHOTS_REPO_USER")
    setFromEnv(
        "connectSnapshotsRepoPassword",
        "CONNECT_SNAPSHOTS_REPO_PASSWORD"
    )
    setFromEnv("connectSnapshotsRepo", "CONNECT_SNAPSHOTS_REPO_URL")
    setFromEnv("snykTokens", "SNYK_TOKENS")
}
