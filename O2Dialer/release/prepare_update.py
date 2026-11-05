"""Create the public update feed only from the APK already verified by the workflow."""
import hashlib
import json
import os
import pathlib
import re

out = pathlib.Path("out")
apk = out / "O2Dialer-7.0-release.apk"
metadata = (out / "apk-metadata.txt").read_text()
code = int(re.search(r"versionCode='(\d+)'", metadata).group(1))
version = re.search(r"versionName='([^']+)'", metadata).group(1)
sdk = int(re.search(r"^sdkVersion:'(\d+)'", metadata, re.M).group(1))
assert code == 100 + int(os.environ["GITHUB_RUN_NUMBER"])
assert "package: name='com.o2dialer.app'" in metadata
assert "application-debuggable" not in metadata
assert re.fullmatch(r"[0-9A-Za-z.+_-]{1,32}", version)
size = apk.stat().st_size
assert 0 < size <= 100 * 1024 * 1024
tag = f"o2dialer-build-{code}"
feed = {
    "schema": 1,
    "packageName": "com.o2dialer.app",
    "versionCode": code,
    "versionName": version,
    "minSdk": sdk,
    "size": size,
    "sha256": hashlib.sha256(apk.read_bytes()).hexdigest(),
    "apkUrl": f"https://github.com/Adsvibz/New/releases/download/{tag}/{apk.name}",
}
(out / "update.json").write_text(json.dumps(feed, indent=2) + "\n")
