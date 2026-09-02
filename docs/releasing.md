# Releasing the SDK

The GitHub Actions release workflow builds the SDK, signs its Maven artifacts,
deploys them to JFrog, creates a release bundle, and promotes that bundle in
order through DEV, TEST, and STAGE.

Only one release runs at a time. In-progress releases are never cancelled;
another tag push or manual dispatch waits until the current run finishes.

## Repository configuration

No repository-level Actions variables are required. These values are hardcoded
in `.github/workflows/release.yml`:

- JFrog project: `connect`
- OIDC provider: `gh-aerospike`
- OIDC audience: `aerospike`

The JFrog platform URL and the runner are left unset so the
`aerospike/shared-workflows` defaults (`https://artifact.aerospike.io` and
`ubuntu-22.04`) apply. The promotion job sets the URL explicitly because the
JFrog CLI action has no default.

Signing uses the organization secrets `GPG_SECRET_KEY`, `GPG_PUBLIC_KEY`, and
`GPG_PASS`. CI uses the organization secret `SNYK_TOKENS`.

The `gh-aerospike` JFrog OIDC mapping must permit this repository to create,
deploy, and promote release bundles in the `connect` project.

## Start a release

Land all changes, including Dependabot, on `stage`. `master` stays on the last
released version. When you are ready to ship, merge `stage` into `master`, then
push a release tag such as `1.4.5`, `v1.4.5`, or `1.4.5-rc1`. Alternatively,
run **Build and promote SDK release** manually and enter the release version.

Bump `gradle.properties` on the feature branch that introduces the next change.
The release workflow uses the tag (or dispatch input) as `-Pversion` and does
not rewrite `gradle.properties`.

The workflow performs:

1. Build Java 11-compatible binaries with a Java 17 Gradle runtime.
2. Generate the main, sources, and Javadoc JARs, the Maven POM, and the Gradle
   Module Metadata (`.module`) file.
3. Sign and deploy them through `aerospike/shared-workflows`.
4. Create the `aerospike-connect-inbound-sdk` JFrog release bundle.
5. Promote the same bundle to DEV, then TEST, then STAGE.

The three promotions are sequential steps in one job. A failed promotion stops
the later promotions.

## Gradle's role in a release

Gradle only produces files; it never publishes them. The `mavenJava`
publication in `buildSrc/.../PublishingExtensions.kt` exists so the release
workflow can run `generatePomFileForMavenJavaPublication` and
`generateMetadataFileForMavenJavaPublication`, then stage the results in
`dist/`. Signing, deployment, bundling, and promotion belong to
`aerospike/shared-workflows`.

`deploy-artifacts` collects Maven companion files by JAR stem, so the POM and
`.module` files are copied as
`aerospike-connect-inbound-sdk-<version>.{pom,module}` to match
`aerospike-connect-inbound-sdk-<version>.jar`. Gradle Module Metadata support
was added in
[shared-workflows#293](https://github.com/aerospike/shared-workflows/pull/293),
which is not in a tagged release yet, so the workflow pins commit
`7c8b65d`. Move the pin to the next tag (after `v4.1.0`) when one ships.

There is no Gradle publishing path to Sonatype or to a snapshot repository, and
no local release script. Releases happen only through the workflow above.

## Maven Central publication

Maven Central publication is owned by
[`citrusleaf/artifact-publisher`](https://github.com/citrusleaf/artifact-publisher).
This repository must not call Sonatype directly.

`artifact-publisher` is triggered automatically by the JFrog webhook after the
release bundle is promoted to **PROD**. STAGE promotion alone does not publish
to Maven Central.

The publisher detects the Maven layout in the release bundle, downloads the
JAR, POM, signatures, and checksums, submits the bundle to the Sonatype
Publisher API, and waits for the deployment to reach `PUBLISHED`.
