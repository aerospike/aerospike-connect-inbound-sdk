# Releasing the SDK

Work lands on `stage`. Merge `stage` into `master` only when cutting a
release. Bump `gradle.properties` on the feature branch that introduces the
change. That file holds the logical line version (for example, `1.4.5`).

Every release uses a unique `-N` suffix on the logical version. The git tag,
JFrog bundle, and Maven GAV are the same (for example, `1.4.5-1`). Plain
`X.Y.Z` tags are rejected.

**Build SDK release to DEV** signs and deploys Maven artifacts, creates the
`aerospike-connect-inbound-sdk` bundle, annotates it with the logical version,
and promotes to DEV. Consume the exact Maven GAV from the connect DEV Maven
repository.

**Promote SDK release bundle** promotes a DEV bundle to TEST, then STAGE, in
one run. PROD is the org approval path;
[`citrusleaf/artifact-publisher`](https://github.com/citrusleaf/artifact-publisher)
publishes to Maven Central after PROD, not STAGE.

JFrog bundle versions are immutable. Each attempt needs a new `-N` suffix:

| Cut     | Tag / bundle / Maven | Annotation |
|---------|----------------------|------------|
| First   | `1.4.5-1`            | `1.4.5`    |
| Rebuild | `1.4.5-2`            | `1.4.5`    |

The release workflow checks that the logical version matches `gradle.properties`
and fails if `-N` is missing.

Gradle only generates the JARs, POM, and `.module` file. Signing, deploy, and
promotion are `aerospike/shared-workflows`. The `.module` file is copied next
to the JAR stem so deploy-artifacts picks it up.
