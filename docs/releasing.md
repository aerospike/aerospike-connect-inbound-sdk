# Releasing the SDK

Work lands on `stage`. `gradle.properties` holds the logical line version (for
example, `1.4.5`); bump it on the feature branch that introduces the change.

QE certifies `-N` bundles built from `stage`. The shippable release is plain
`X.Y.Z` at the **same git commit** as the highest `-N` that QE certified
(commit-id similarity, not a byte-identical rebuild).

| Kind | Tag / bundle / Maven | From | Annotation |
|------|----------------------|------|------------|
| QE first | `1.4.5-1` | `stage` squash commit | `1.4.5` |
| QE rebuild | `1.4.5-2` | `stage` squash commit | `1.4.5` |
| GA | `1.4.5` | same commit as highest `-N` (must be on `master`) | `1.4.5` |

**Before each `-N` tag**, squash `stage` onto `master` into a single commit
(see the `sdk-release` skill). Then tag `X.Y.Z-N` on that commit and run
**Build SDK release to DEV**.

**After QE certifies**, open a PR `stage` → `master` and use **Create a merge
commit** (not squash, not rebase). That keeps the squash commit as a parent of
`master`. Tag `X.Y.Z` on **that parent** (the same SHA as the highest `-N`),
not on `master` HEAD. The workflow rejects a GA tag that is not reachable from
`master` or that does not match that certified commit.

**Build SDK release to DEV** signs and deploys Maven artifacts, creates the
`aerospike-connect-inbound-sdk` bundle, annotates it with the logical version,
and promotes to DEV. Consume the exact Maven GAV from the connect DEV Maven
repository.

**Promote SDK release bundle** promotes a DEV bundle to TEST, then STAGE, in
one run. PROD is the org approval path;
[`citrusleaf/artifact-publisher`](https://github.com/citrusleaf/artifact-publisher)
publishes to Maven Central after PROD, not STAGE.

JFrog bundle versions are immutable. Each QE attempt needs a new `-N`. The GA
version `X.Y.Z` is a separate bundle. The release workflow checks that
`gradle.properties` matches the logical version.

Gradle only generates the JARs, POM, and `.module` file. Signing, deploy, and
promotion are `aerospike/shared-workflows`. The `.module` file is copied next
to the JAR stem so deploy-artifacts picks it up.
