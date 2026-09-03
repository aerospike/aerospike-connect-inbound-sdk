# Releasing the SDK

Work lands on `stage`. Merge `stage` into `master` only when cutting a
release. Bump `gradle.properties` on the feature branch that introduces the
change. The release workflow uses the tag or dispatch input as `-Pversion`.

**Build SDK release to DEV** signs and deploys Maven artifacts, creates the
`aerospike-connect-inbound-sdk` bundle, annotates it, and promotes to DEV.
Consume that exact GAV from the connect DEV Maven repository.

**Promote SDK release bundle** promotes a DEV bundle to TEST, then STAGE, in
one run. PROD is the org approval path;
[`citrusleaf/artifact-publisher`](https://github.com/citrusleaf/artifact-publisher)
publishes to Maven Central after PROD, not STAGE.

JFrog bundle versions are immutable. Rebuilds need a new version:

| Cut | Bundle / JAR | Annotation |
| --- | --- | --- |
| First RC | `1.4.5-rc1` | `1.4.5-rc1` |
| Rebuild | `1.4.5-rc1-build01` | `1.4.5-rc1` |
| Final | `1.4.5` | `1.4.5` |

Gradle only generates the JARs, POM, and `.module` file. Signing, deploy, and
promotion are `aerospike/shared-workflows`. The `.module` file is copied next
to the JAR stem so deploy-artifacts picks it up.
