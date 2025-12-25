#!/usr/bin/env bash
# File: `scripts/publish-release.sh`
# Usage: ./scripts/publish-release.sh [VERSION]
# Creates git tag vVERSION, pushes it, and creates a GitHub release.
# Requires: git, curl (and either gh CLI or GITHUB_TOKEN env var)

set -euo pipefail

# Helper: get latest v* tag (most recent by semantic sort)
get_latest_tag() {
  git tag --list 'v*' --sort=-v:refname 2>/dev/null | head -n1 || true
}

ARG_VERSION="${1:-}"
REMOTE="${2:-origin}"

# Ensure we have a clean working tree (optional safety)
if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "Working tree is dirty. Commit or stash changes before creating a release."
  exit 1
fi

CUR_TAG=$(get_latest_tag)
if [[ -z "${CUR_TAG}" ]]; then
  CUR_VERSION="none"
else
  # strip leading v
  CUR_VERSION="${CUR_TAG#v}"
fi

# Determine default version
if [[ -n "${ARG_VERSION}" ]]; then
  DEFAULT_VERSION="${ARG_VERSION}"
else
  if [[ "${CUR_VERSION}" == "none" ]]; then
    DEFAULT_VERSION="1.0.0"
  else
    IFS='.' read -r MAJOR MINOR PATCH <<<"${CUR_VERSION}"
    # If parse fails, fallback
    if [[ -z "${PATCH:-}" ]]; then
      DEFAULT_VERSION="1.0.0"
    else
      PATCH=$((PATCH + 1))
      DEFAULT_VERSION="${MAJOR}.${MINOR}.${PATCH}"
    fi
  fi
fi

echo "Current version: ${CUR_VERSION}"
read -r -p "New version [${DEFAULT_VERSION}]: " INPUT_VERSION
if [[ -n "${INPUT_VERSION}" ]]; then
  VERSION="${INPUT_VERSION}"
else
  VERSION="${DEFAULT_VERSION}"
fi

TAG="v${VERSION}"

# Prompt to edit changelog
CHANGELOG="CHANGELOG.md"
read -r -p "Edit ${CHANGELOG} now to add notes for v${VERSION}? [Y/n] " EDIT_CHOICE
EDIT_CHOICE="${EDIT_CHOICE:-Y}"
if [[ "${EDIT_CHOICE}" =~ ^[Yy] ]]; then
  if [[ ! -f "${CHANGELOG}" ]]; then
    cat > "${CHANGELOG}" <<EOF
# Changelog

All notable changes to this project will be documented in this file.

## ${TAG} - $(date +%F)

- Describe changes here.

EOF
  else
    # Prepend a header for the new version to the top for convenience
    TMPFILE="$(mktemp)"
    {
      echo "## ${TAG} - $(date +%F)"
      echo
      echo "- Describe changes here."
      echo
      cat "${CHANGELOG}"
    } > "${TMPFILE}"
    mv "${TMPFILE}" "${CHANGELOG}"
  fi

  # Prefer nano for editing the changelog if available; otherwise fall back to EDITOR/VISUAL or vi.
  if command -v nano >/dev/null 2>&1; then
    EDITOR="nano"
  else
    : "${EDITOR:=${VISUAL:-vi}}"
  fi
  "${EDITOR}" "${CHANGELOG}"
else
  echo "Make sure to update ${CHANGELOG} with notes for ${TAG} before publishing."
fi

# Create annotated tag if it doesn't exist
if git rev-parse "${TAG}" >/dev/null 2>&1; then
  echo "Tag ${TAG} already exists locally."
else
  git tag -a "${TAG}" -m "Release ${VERSION}"
  echo "Created tag ${TAG}."
fi

# Push tag
git push "${REMOTE}" "${TAG}"
echo "Pushed tag ${TAG} to ${REMOTE}."

# Try gh CLI first
if command -v gh >/dev/null 2>&1; then
  echo "Using gh CLI to create release..."
  gh release create "${TAG}" --title "v${VERSION}" --notes-file "${CHANGELOG}" --notes "Release ${VERSION}"
  echo "Release v${VERSION} created via gh."
  exit 0
fi

# Fallback to GitHub API using GITHUB_TOKEN
if [[ -z "${GITHUB_TOKEN:-}" ]]; then
  echo "gh CLI not found and GITHUB_TOKEN is not set. Install gh or set GITHUB_TOKEN."
  exit 1
fi

# Determine repo owner/name from origin url
ORIGIN_URL=$(git remote get-url "${REMOTE}")
# Support git@github.com:owner/repo.git and https://github.com/owner/repo.git
if [[ "${ORIGIN_URL}" =~ ^git@github.com:(.+)/(.+)\.git$ ]]; then
  OWNER="${BASH_REMATCH[1]}"
  REPO="${BASH_REMATCH[2]}"
elif [[ "${ORIGIN_URL}" =~ ^https://github.com/(.+)/(.+)\.git$ ]]; then
  OWNER="${BASH_REMATCH[1]}"
  REPO="${BASH_REMATCH[2]}"
else
  echo "Unable to parse GitHub repo from origin URL: ${ORIGIN_URL}"
  exit 1
fi

API_URL="https://api.github.com/repos/${OWNER}/${REPO}/releases"

# Use changelog content if available for body
if [[ -f "${CHANGELOG}" ]]; then
  # Extract the top section for this version (until next "## " header) to include as body
  RELEASE_BODY="$(awk '/^## /{if (c++==0) {print; next} else exit} {if (c==0) print}' "${CHANGELOG}" | sed '1d' || true)"
else
  RELEASE_BODY="Release ${VERSION}"
fi

# JSON encode simple body (safe for most contents)
POST_DATA=$(cat <<EOF
{
  "tag_name": "${TAG}",
  "name": "v${VERSION}",
  "body": $(printf '%s' "${RELEASE_BODY}" | python3 -c 'import json,sys; print(json.dumps(sys.stdin.read()))'),
  "draft": false,
  "prerelease": false
}
EOF
)

echo "Creating release via GitHub API for ${OWNER}/${REPO}..."
HTTP_RESPONSE=$(curl -sS -o /dev/stderr -w "%{http_code}" \
  -X POST "${API_URL}" \
  -H "Authorization: token ${GITHUB_TOKEN}" \
  -H "Accept: application/vnd.github+json" \
  -d "${POST_DATA}" 2>/dev/null || true)

if [[ "${HTTP_RESPONSE}" == "201" ]]; then
  echo "Release v${VERSION} created successfully."
  exit 0
else
  echo "Failed to create release (HTTP ${HTTP_RESPONSE})."
  exit 1
fi
