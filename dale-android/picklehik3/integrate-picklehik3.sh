#!/usr/bin/env bash
set -euo pipefail

SRC_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PICKLE_ROOT="${1:?usage: $0 <picklehik3-root> [nethunter.svg]}"
SVG_SOURCE="${2:-}"

MAIN="${PICKLE_ROOT}/app/src/main"
JAVA_DIR="${MAIN}/java/com/termux/app"
MANIFEST="${MAIN}/AndroidManifest.xml"
ASSET_DIR="${MAIN}/assets/dale"
JAVA_SOURCE="${SRC_ROOT}/dale-android/picklehik3/DaleLauncherActivity.java"

[[ -d "${PICKLE_ROOT}/.git" ]] || { echo "Not a git checkout: ${PICKLE_ROOT}" >&2; exit 1; }
[[ -f "${MANIFEST}" ]] || { echo "Manifest not found: ${MANIFEST}" >&2; exit 1; }
[[ -f "${JAVA_SOURCE}" ]] || { echo "Launcher source missing: ${JAVA_SOURCE}" >&2; exit 1; }

mkdir -p "${JAVA_DIR}" "${ASSET_DIR}"
cp "${JAVA_SOURCE}" "${JAVA_DIR}/DaleLauncherActivity.java"

if [[ -n "${SVG_SOURCE}" ]]; then
  [[ -f "${SVG_SOURCE}" ]] || { echo "SVG not found: ${SVG_SOURCE}" >&2; exit 1; }
  cp "${SVG_SOURCE}" "${ASSET_DIR}/nethunter-original.svg"

  python3 - "${ASSET_DIR}/nethunter-original.svg" <<'PY'
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

p = Path(sys.argv[1])
data = p.read_text(encoding="utf-8")
root = ET.fromstring(data)

if root.tag.split("}")[-1] != "svg":
    raise SystemExit("Root element is not <svg>")

if "width" not in root.attrib or "height" not in root.attrib:
    raise SystemExit("SVG is missing width/height attributes")

print(f"SVG OK: {p} ({root.attrib.get('width')} x {root.attrib.get('height')})")
PY
fi

cp "${MANIFEST}" "${MANIFEST}.dale-prelauncher.bak"

python3 - "${MANIFEST}" <<'PY'
from pathlib import Path
import re
import sys

p = Path(sys.argv[1])
text = p.read_text(encoding="utf-8")

if 'android:name=".app.DaleLauncherActivity"' in text:
    print("DALE launcher already present; no manifest change needed.")
    raise SystemExit(0)

activity_re = re.compile(
    r'(?P<block><activity\s+android:name="\.app\.TermuxActivity"\b[\s\S]*?</activity>)'
)
m = activity_re.search(text)
if not m:
    raise SystemExit("Could not locate .app.TermuxActivity in manifest")

block = m.group("block")
launcher_filter = re.compile(
    r'\s*<intent-filter>\s*'
    r'<action\s+android:name="android\.intent\.action\.MAIN"\s*/>\s*'
    r'<category\s+android:name="android\.intent\.category\.LAUNCHER"\s*/>\s*'
    r'</intent-filter>\s*'
)
new_block, removed = launcher_filter.subn("\n", block, count=1)
if removed != 1:
    raise SystemExit("Could not remove the TermuxActivity MAIN/LAUNCHER filter")

dale_activity = '''    <activity
            android:name=".app.DaleLauncherActivity"
            android:exported="true"
            android:label="@string/application_name"
            android:launchMode="singleTask"
            android:theme="@style/Theme.TermuxActivity.DayNight.NoActionBar"
            android:windowSoftInputMode="stateAlwaysHidden">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

'''

text = text[:m.start()] + dale_activity + new_block + text[m.end():]
p.write_text(text, encoding="utf-8")
print("Manifest patched: DALE is MAIN/LAUNCHER; TermuxActivity remains the execution activity.")
PY

echo
echo "DALE PickleHik3 integration prepared."
echo "Java:   ${JAVA_DIR}/DaleLauncherActivity.java"
echo "Asset:  ${ASSET_DIR}/nethunter-original.svg"
echo "Backup: ${MANIFEST}.dale-prelauncher.bak"
echo
echo "Next:"
echo "  cd ${PICKLE_ROOT}"
echo "  ./gradlew --no-daemon --max-workers=2 assembleDebug -x lint --stacktrace"
