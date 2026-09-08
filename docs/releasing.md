# Releasing the SDK

Work lands on `master` (squash-merge pull requests). `gradle.properties` holds
the logical line version (for example, `1.4.5`); bump it on the feature branch
that introduces the change.

QE certifies `-N` bundles tagged on `master`. The shippable release is plain
`X.Y.Z` at the **same git commit** as the highest `-N` on `master` that QE
certified (commit-id similarity, not a byte-identical rebuild). If QE rejects
a build, leave `master` as-is, land a fix, and tag a new N. Do not rewrite
`master`.

| Kind | Tag / bundle / Maven | From | Annotation |
|------|----------------------|------|------------|
| Dev try-out | `1.4.5-1` | any branch (stays in DEV) | `1.4.5` |
| QE | `1.4.5-2` | `master` | `1.4.5` |
| GA | `1.4.5` | same commit as highest `-N` on `master` | `1.4.5` |

A `-N` tag may be pushed from any branch, so a dev can get a DEV bundle without
touching `master`. Pick an unused N yourself (`git tag 1.4.5-3 && git push
origin 1.4.5-3`). **Promote SDK release bundle** is the gate: it requires the
tag named after the bundle version to be on `master`, so a bundle built off a
dev branch cannot go past DEV.

**QE:** squash-merge the work to `master`, tag `X.Y.Z-N` on that commit, and
run **Build SDK release to DEV**. Promote with the TEST/STAGE checkboxes. If QE fails, merge
the fix to `master` and repeat with the next N.

**GA:** after QE accepts, tag `X.Y.Z` on the same SHA as that `-N` (usually
current `master` HEAD). The workflow rejects a GA tag that is not on `master`
or that does not match the highest `-N` already on `master`.

**Build SDK release to DEV** signs and deploys Maven artifacts, creates the
`aerospike-connect-inbound-sdk` bundle, annotates it with the logical version,
and promotes to DEV. Consume the exact Maven GAV from the connect DEV Maven
repository.

**Promote SDK release bundle** asks which environments to move. TEST only
stops at TEST. STAGE promotes TEST first unless that version is already on
TEST, then promotes STAGE. PROD is the org approval path;
[`citrusleaf/artifact-publisher`](https://github.com/citrusleaf/artifact-publisher)
publishes to Maven Central after PROD, not STAGE.

JFrog bundle versions are immutable. Each QE attempt needs a new `-N`. The GA
version `X.Y.Z` is a separate bundle. The release workflow checks that
`gradle.properties` matches the logical version.

Gradle only generates the JARs, POM, and `.module` file. Signing, deploy, and
promotion are `aerospike/shared-workflows`. The `.module` file is copied next
to the JAR stem so deploy-artifacts picks it up.
