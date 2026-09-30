#!/usr/bin/env bash
set -euo pipefail

ROOT="${1:?usage: install-launcher.sh <android-app-src-main>}"
PKG="${ROOT}/java/com/termux/app"
MANIFEST="${ROOT}/AndroidManifest.xml"
REPO_ROOT="${2:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"

mkdir -p "${PKG}"
cp "${REPO_ROOT}/dale-android/src/main/java/com/termux/app/DaleLauncherActivity.java" "${PKG}/DaleLauncherActivity.java"

test -s "${PKG}/DaleLauncherActivity.java"
grep -q 'DALE CORE' "${PKG}/DaleLauncherActivity.java"
grep -q 'TermuxActivity.class' "${PKG}/DaleLauncherActivity.java"
test -f "${ROOT}/java/com/termux/app/TermuxActivity.java"

python3 - "${MANIFEST}" <<'PY'
from pathlib import Path
import sys

manifest = Path(sys.argv[1])
text = manifest.read_text()

launcher = """            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>"""

if launcher not in text:
    raise SystemExit("Termux MAIN/LAUNCHER filter not found")

text = text.replace(launcher, "", 1)

anchor = """        <activity
            android:name=".app.TermuxActivity""""

insert = """        <activity
            android:name=".app.DaleLauncherActivity"
            android:exported="true"
            android:screenOrientation="portrait"
            android:theme="@style/Theme.TermuxActivity.DayNight.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

"""

if anchor not in text:
    raise SystemExit("TermuxActivity manifest anchor not found")

text = text.replace(anchor, insert + anchor, 1)
manifest.write_text(text)
PY

grep -q 'android:name=".app.DaleLauncherActivity"' "${MANIFEST}"
! grep -A8 -B1 'android:name=".app.TermuxActivity"' "${MANIFEST}" | grep -q 'category android:name="android.intent.category.LAUNCHER"'
echo "DALE launcher installed from committed source."
