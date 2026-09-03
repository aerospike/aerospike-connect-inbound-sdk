# Releasing the SDK

**Build SDK release to DEV** builds the SDK, signs its Maven artifacts, deploys
them to JFrog, creates a release bundle, and promotes that bundle to **DEV**
only. Consume that JAR from the connect DEV Maven repository.

**Promote SDK release bundle** is a separate manual workflow that promotes an
existing DEV bundle to TEST, then STAGE, in one run. A failed TEST promotion
stops STAGE.

Only one DEV release runs at a time. In-progress releases are never cancelled;
another tag push or manual dispatch waits until the current run finishes.

## Repository configuration

No repository-level Actions variables are required. These values are hardcoded
in the workflows:

- JFrog project: `connect`
- OIDC provider: `gh-aerospike`
- OIDC audience: `aerospike`

The JFrog platform URL and the runner are left unset so the
`aerospike/shared-workflows` defaults (`https://artifact.aerospike.io` and
`ubuntu-22.04`) apply. Jobs that call `setup-jfrog-cli` set the URL explicitly
because that action has no default.

Signing uses the organization secrets `GPG_SECRET_KEY`, `GPG_PUBLIC_KEY`, and
`GPG_PASS`. CI uses the repository secret `SNYK_TOKEN`.

The `gh-aerospike` JFrog OIDC mapping must permit this repository to create,
deploy, and promote release bundles in the `connect` project.

## Unique bundle versions (Spark incremental model)

JFrog does not allow deleting a release bundle and recreating the same
name + version. `aerospike-spark` keeps rebuilds unique with tags such as
`v5.0.4_spark35_build01`, then annotates the bundle with the logical version
(`5.0.4_spark35`). This SDK does the same with Maven-legal qualifiers:

| What you cut | Bundle and JAR version | Annotation tag |
| --- | --- | --- |
| First candidate | `1.4.5-rc1` | `1.4.5-rc1` |
| Rebuild of that candidate | `1.4.5-rc1-build01` | `1.4.5-rc1` |
| Next candidate | `1.4.5-rc2` | `1.4.5-rc2` |
| Final | `1.4.5` | `1.4.5` |
| Rebuild of the final | `1.4.5-build01` | `1.4.5` |

Never reuse a version that already has a bundle. Gradle `-Pversion` and the
JFrog bundle version are the same string, so a consuming project depends on
the exact unique version that landed in DEV.

## Start a DEV release

Land all changes, including Dependabot, on `stage`. `master` stays on the last
released version. When you are ready to ship a candidate, merge `stage` into
`master`, then push a unique tag such as `1.4.5-rc1` or `v1.4.5-rc1-build01`.
Alternatively, run **Build SDK release to DEV** and enter that version.

Bump `gradle.properties` on the feature branch that introduces the next change.
The release workflow uses the tag (or dispatch input) as `-Pversion` and does
not rewrite `gradle.properties`.

The DEV workflow performs:

1. Build Java 11-compatible binaries with a Java 17 Gradle runtime.
2. Generate the main, sources, and Javadoc JARs, the Maven POM, and the Gradle
   Module Metadata (`.module`) file.
3. Sign and deploy them through `aerospike/shared-workflows`.
4. Create the `aerospike-connect-inbound-sdk` JFrog release bundle.
5. Annotate the bundle with the logical version (build suffix stripped).
6. Promote that bundle to DEV.

## Promote to TEST and STAGE

After the DEV JAR is verified in a consuming project, run **Promote SDK
release bundle** and enter the exact bundle version. The job promotes to TEST,
then STAGE. PROD stays on the organization approval path, not this workflow.

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
no local release script. Releases happen only through the workflows above.

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
