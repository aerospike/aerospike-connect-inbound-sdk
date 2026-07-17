package com.aerospike.connect

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import java.util.Base64

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
    setFromEnv("ossrhUsername", "OSSRH_USERNAME")
    setFromEnv("ossrhPassword", "OSSRH_PASSWORD")
    setFromEnv("signing.keyId", "SIGNING_KEY_ID")
    setFromEnv("signing.password", "SIGNING_PASSWORD")
    setSigningKeyFromBase64Env("SIGNING_SECRET_KEY_BASE64")
}

private fun Project.setSigningKeyFromBase64Env(envVar: String) {
    val encoded = System.getenv(envVar) ?: return
    extra["signing.secretKey"] = String(
        Base64.getDecoder().decode(encoded.replace("\\s".toRegex(), "")),
        Charsets.UTF_8
    )
}
