---
name: sdk-release
description: >-
  Squash every commit on stage since master into one commit, keeping the
  original subjects and bodies. Use when the user is on stage and asks to
  squash, fold, or collapse stage commits before a QE -N tag.
disable-model-invocation: true
---

# Squash stage for QE

When this skill is invoked, the user is on `stage` and wants **one** commit
versus `origin/master`, with the squashed commits' messages kept. Do that and
nothing else unless they also ask to tag or merge.

Follow `docs/releasing.md` for the rest of the release (QE `-N` on this
commit; later **Create a merge commit** into `master`; GA tag `X.Y.Z` on this
same SHA, not on `master` HEAD).

## Squash

1. Refuse unless `git branch --show-current` is `stage`, the working tree is
   clean, and you are not mid-rebase/merge.
2. `git fetch origin`. `origin/master` must be an ancestor of `HEAD`. If not,
   stop: merge or rebase `master` into `stage` first so the QE tree includes
   `master`.
3. If `HEAD` equals `origin/master`, stop (nothing to squash). If there is
   already exactly one commit on top of `origin/master`, stop and say so.
4. Collect messages, oldest first:

```bash
git log --reverse --format='%s%n%n%b' origin/master..HEAD
```

5. Subject: use the first line of the oldest commit if it names the work;
   otherwise a short line that includes the ticket ids from those subjects.
   Body: the full log from step 4 (subjects and bodies). Do not invent tickets
   or drop Co-authored-by trailers.
6. Show the proposed message. Then:

```bash
git reset --soft origin/master
git commit -F - <<'EOF'
<proposed message>
EOF
```

Do not use `git rebase -i`. Do not amend. Do not force-push `master`.

7. Show `git log -1 --format=fuller`. Push with
   `git push --force-with-lease origin stage` only after the user confirms
   (this invocation is a rewrite of `stage`, but the push still needs an
   explicit yes).

## After squash (only if asked)

- Tag QE: `git tag <X.Y.Z-N> && git push origin <X.Y.Z-N>` from this commit.
- GA: after the merge-commit PR, `git tag <X.Y.Z> <highest-N-tag>` and push.
  Never tag GA at the merge commit (`master` HEAD).
