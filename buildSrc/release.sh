#!/usr/bin/env bash

#
#
#  Copyright 2012-2025 Aerospike, Inc.
#
#  Portions may be licensed to Aerospike, Inc. under one or more contributor
#  license agreements WHICH ARE COMPATIBLE WITH THE APACHE LICENSE, VERSION 2.0.
#
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not
#  use this file except in compliance with the License. You may obtain a copy of
#  the License at http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#  License for the specific language governing permissions and limitations under
#  the License.
#

set -e

usage() {
  cat <<EOF
usage: bash release.sh --version 1.1.0 --release-notes-file release-notes.md

  -v  (Required)          Version
  -n  (Required)          Path of release notes files
  -h                      Print usage help

Requires github credentials as environment variables GITHUB_USERNAME and GITHUB_TOKEN
EOF
}

while getopts m:v:n:h opt; do
  # shellcheck disable=SC2220
  case "$opt" in
  v)
    version=${OPTARG}
    ;;
  n)
    releaseNotesFile=${OPTARG}
    ;;
  h)
    usage
    exit 0
    ;;
  esac
done

if [ -z "$version" ]; then
  echo "Release version is required"
  exit
fi

if [ -z "$releaseNotesFile" ]; then
  echo "Release notes file is required"
  exit
fi

if [ -z "$GITHUB_USERNAME" ]; then
  echo "Github username environment variable GITHUB_USERNAME not set".
  exit
fi

if [ -z "$GITHUB_TOKEN" ]; then
  echo "Github access token environment variable GITHUB_TOKEN not set".
  exit
fi

echo "--------------------------------------------------------------------------"
echo "Releasing aerospike-connect-inbound-sdk version:$version"
echo "Args release-notes-file:$releaseNotesFile"
echo "--------------------------------------------------------------------------"

# Run vulnerability scan on the module
./gradlew --stacktrace --no-daemon ":snyk-test"

# Run the release task
../gradlew --stacktrace --no-daemon release publishGithubRelease -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=$version -PreleaseNotesFile="$releaseNotesFile"
