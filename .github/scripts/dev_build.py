#!/usr/bin/env python3
"""The dev build publishing steps of .github/workflows/dev-build.yml.

Every workflow step is its own process, so the steps hand data to each other
through files in the working directory.

Usage: python3 .github/scripts/dev_build.py <step>
"""

import json
import os
import subprocess
import sys
from pathlib import Path

COMMITS_FILE = Path("commits.json")
CHANGELOG_FILE = Path("changelog.txt")
METADATA_FILE = Path("release-metadata.json")


def build_changelog():
    """Save the pushed commits to commits.json and changelog.txt."""
    commits = read_json(os.environ["GITHUB_EVENT_PATH"])["commits"]

    write_json(COMMITS_FILE, [{"sha": c["id"], "message": c["message"]} for c in commits])

    changelog = "\n\n".join(format_commit(c) for c in commits)
    if not changelog:
        changelog = f"Build {os.environ['GITHUB_RUN_NUMBER']}"
    CHANGELOG_FILE.write_text(changelog + "\n", encoding="utf-8")


def format_commit(commit):
    """Format a commit as a list entry, with later message lines indented under the first."""
    message = commit["message"].replace("\r\n", "\n").replace("\n", "\n  ")
    return f"- {message} ({commit['id'][:7]})"


def read_release_metadata():
    """Find the built JAR and the Minecraft versions it supports, and save them."""
    plugin = os.environ["PLUGIN_NAME"]

    jars = sorted(Path("build/libs").glob(f"{plugin}-*.jar"))
    if not jars:
        fail(f"No {plugin} JAR found in build/libs")
    jar = jars[0]

    minecraft_versions = read_supported_minecraft_versions()
    if not minecraft_versions:
        fail("supportedMinecraftVersions is empty in gradle.properties")

    write_json(METADATA_FILE, {
        "artifact_path": str(jar),
        "plugin_version": jar.stem.removeprefix(f"{plugin}-"),
        "supported_minecraft_versions": minecraft_versions,
    })


def read_supported_minecraft_versions():
    """Read the comma-separated supportedMinecraftVersions from gradle.properties."""
    for line in Path("gradle.properties").read_text(encoding="utf-8").splitlines():
        key, _, value = line.partition("=")
        if key.strip() == "supportedMinecraftVersions":
            return [version.strip() for version in value.split(",") if version.strip()]
    return []


def upload_to_server():
    """Send the JAR and the build's details to our server."""
    metadata = read_json(METADATA_FILE)

    payload = {
        "secret": os.environ["SERVER_SECRET"],
        "repo": os.environ["GITHUB_REPOSITORY"],
        "build_number": int(os.environ["GITHUB_RUN_NUMBER"]),
        "branch": os.environ["GITHUB_REF_NAME"],
        "commits": read_json(COMMITS_FILE),
        "supported_minecraft_versions": metadata["supported_minecraft_versions"],
    }

    curl(
        "-X", "POST", os.environ["SERVER_UPLOAD_URL"],
        "-F", "payload=<-",
        "-F", f"file=@{metadata['artifact_path']}",
        stdin=json.dumps(payload),
    )


def upload_to_pixeleast():
    """Publish the JAR to PixelEast as a new dev version of the plugin."""
    token = os.environ["PIXELEAST_TOKEN"]
    if not token:
        fail("PIXELEAST_TOKEN secret is not configured")

    product_id = os.environ["PIXELEAST_PRODUCT_ID"]
    metadata = read_json(METADATA_FILE)

    args = [
        "--fail-with-body",
        "-X", "POST", f"https://api.pixeleast.com/v1/product/{product_id}/versions",
        "-H", f"Authorization: Bearer {token}",
        "-H", "Accept: application/json",
        "-F", f"name={metadata['plugin_version']}",
        "-F", f"file=@{metadata['artifact_path']}",
        "-F", f"changelog=<{CHANGELOG_FILE}",
        "-F", "is_dev=true",
    ]
    for minecraft_version in metadata["supported_minecraft_versions"]:
        args += ["-F", f"selected_versions[]={minecraft_version}"]

    curl(*args)


def curl(*args, stdin=None):
    """Run curl, and fail the step if the request could not be made."""
    result = subprocess.run(["curl", "--silent", "--show-error", *args], input=stdin, text=True)
    if result.returncode != 0:
        sys.exit(result.returncode)


def read_json(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def write_json(path, data):
    path.write_text(json.dumps(data, indent=2), encoding="utf-8")


def fail(message):
    """Show the message as an error annotation on the workflow run and fail the step."""
    print(f"::error::{message}")
    sys.exit(1)


STEPS = {
    "changelog": build_changelog,
    "metadata": read_release_metadata,
    "upload-server": upload_to_server,
    "upload-pixeleast": upload_to_pixeleast,
}

if __name__ == "__main__":
    STEPS[sys.argv[1]]()
