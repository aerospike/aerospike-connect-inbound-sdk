---
name: sdk-release
description: >-
  Tag QE X.Y.Z-N or GA X.Y.Z on master. Use when the user asks to cut a
  release tag after work is squash-merged to master.
disable-model-invocation: true
---

# Tag SDK release on master

Work is already on `master` (GitHub squash-merge). Do not squash or
force-push `master`. Follow `docs/releasing.md`.

## QE (only if asked)

1. Refuse unless `git branch --show-current` is `master`, the working tree is
   clean, and `HEAD` matches `origin/master`.
2. `git fetch origin --tags`. Confirm an unused `X.Y.Z-N` for the version in
   `gradle.properties`.
3. Show the tag and SHA. After the user confirms:

```bash
git tag <X.Y.Z-N>
git push origin <X.Y.Z-N>
```

If QE later rejects that bundle, land a fix on `master` and tag the next N.
Do not reset or force-push `master`.

## GA (only if asked)

Tag `X.Y.Z` at the same commit as the highest `-N` whose commit is on
`master` (usually `master` HEAD after that `-N`).

```bash
git tag <X.Y.Z> <highest-N-tag>
git push origin <X.Y.Z>
```
