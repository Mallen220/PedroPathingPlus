#!/usr/bin/env bash
# File: scripts/publish-release.sh
# Usage: ./scripts/publish-release.sh [VERSION]
# Creates git tag vVERSION, pushes it, and creates a GitHub release.
# Requires: git, curl (and either gh CLI or GITHUB_TOKEN env var)

set -euo pipefail

VERSION="${1:-1.0.0}"
TAG="v${VERSION}"
REMOTE="${2:-origin}"

# Ensure we have a clean working tree (optional safety)
if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "Working tree is dirty. Commit or stash changes before creating a release."
  exit 1
fi

# Create annotated tag if it doesn't exist
if git rev-parse "$TAG" >/dev/null 2>&1; then
  echo "Tag $TAG already exists locally."
else
  git tag -a "$TAG" -m "Release $VERSION"
  echo "Created tag $TAG."
fi

# Push tag
git push "$REMOTE" "$TAG"
echo "Pushed tag $TAG to $REMOTE."

# Try gh CLI first
if command -v gh >/dev/null 2>&1; then
  echo "Using gh CLI to create release..."
  gh release create "$TAG" --title "v${VERSION}" --notes "Release ${VERSION}"
  echo "Release v${VERSION} created via gh."
  exit 0
fi

# Fallback to GitHub API using GITHUB_TOKEN
if [[ -z "${GITHUB_TOKEN:-}" ]]; then
  echo "gh CLI not found and GITHUB_TOKEN is not set. Install gh or set GITHUB_TOKEN."
  exit 1
fi

# Determine repo owner/name from origin url
ORIGIN_URL=$(git remote get-url "$REMOTE")
# Support git@github.com:owner/repo.git and https://github.com/owner/repo.git
if [[ "$ORIGIN_URL" =~ ^git@github.com:(.+)/(.+)\.git$ ]]; then
  OWNER="${BASH_REMATCH[1]}"
  REPO="${BASH_REMATCH[2]}"
elif [[ "$ORIGIN_URL" =~ ^https://github.com/(.+)/(.+)\.git$ ]]; then
  OWNER="${BASH_REMATCH[1]}"
  REPO="${BASH_REMATCH[2]}"
else
  echo "Unable to parse GitHub repo from origin URL: $ORIGIN_URL"
  exit 1
fi

API_URL="https://api.github.com/repos/${OWNER}/${REPO}/releases"
POST_DATA=$(cat <<EOF
{
  "tag_name": "${TAG}",
  "name": "v${VERSION}",
  "body": "Release ${VERSION}",
  "draft": false,
  "prerelease": false
}
EOF
)

echo "Creating release via GitHub API for ${OWNER}/${REPO}..."
HTTP_RESPONSE=$(curl -sS -o /dev/stderr -w "%{http_code}" \
  -X POST "$API_URL" \
  -H "Authorization: token ${GITHUB_TOKEN}" \
  -H "Accept: application/vnd.github+json" \
  -d "$POST_DATA" 2>/dev/null || true)

if [[ "$HTTP_RESPONSE" == "201" ]]; then
  echo "Release v${VERSION} created successfully."
  exit 0
else
  echo "Failed to create release (HTTP $HTTP_RESPONSE)."
  exit 1
fi
