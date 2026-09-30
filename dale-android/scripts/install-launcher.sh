#!/usr/bin/env bash
set -euo pipefail

ROOT="${1:?usage: install-launcher.sh <android-app-src-main>}"
PKG="${ROOT}/java/com/termux/app"
MANIFEST="${ROOT}/AndroidManifest.xml"
REPO_ROOT="${2:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
TERMUX_ACTIVITY="${PKG}/TermuxActivity.java"

mkdir -p "${PKG}"
cp "${REPO_ROOT}/dale-android/src/main/java/com/termux/app/DaleLauncherView.java" "${PKG}/DaleLauncherView.java"
test -s "${PKG}/DaleLauncherView.java"
test -f "${TERMUX_ACTIVITY}"

grep -q 'class TermuxActivity' "${TERMUX_ACTIVITY}"
grep -q 'DALE CORE' "${PKG}/DaleLauncherView.java"

python3 - "${TERMUX_ACTIVITY}" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
text = path.read_text()
anchor = "        setContentView(R.layout.activity_termux);"
call = "        setContentView(R.layout.activity_termux);\n        installDaleLauncherOverlay();"
if "installDaleLauncherOverlay();" not in text:
    if anchor not in text:
        raise SystemExit("TermuxActivity setContentView anchor not found")
    text = text.replace(anchor, call, 1)

method = r'''

    /** Hosts the DALE launcher in the same activity as the terminal, like termux-launcher. */
    private void installDaleLauncherOverlay() {
        android.view.View content = findViewById(android.R.id.content);
        if (!(content instanceof android.view.ViewGroup)) return;
        android.view.ViewGroup host = (android.view.ViewGroup) content;
        com.termux.app.DaleLauncherView.attach(host, new com.termux.app.DaleLauncherView.Listener() {
            @Override public void onTerminal() {
                android.view.View launcher = host.findViewWithTag("dale_launcher_overlay");
                if (launcher != null) host.removeView(launcher);
                if (mTerminalView != null) mTerminalView.requestFocus();
            }

            @Override public void onSettings() {
                startActivity(new android.content.Intent(TermuxActivity.this, com.termux.app.activities.SettingsActivity.class));
            }
        });
        android.view.View launcher = host.getChildAt(host.getChildCount() - 1);
        if (launcher != null) launcher.setTag("dale_launcher_overlay");
    }
'''
if "private void installDaleLauncherOverlay()" not in text:
    marker = "\n}"
    index = text.rfind(marker)
    if index < 0:
        raise SystemExit("TermuxActivity class closing brace not found")
    text = text[:index] + method + text[index:]
path.write_text(text)
PY

# Keep TermuxActivity as the sole MAIN/LAUNCHER activity.
python3 - "${MANIFEST}" <<'PY'
from pathlib import Path
import sys
text = Path(sys.argv[1]).read_text()
if 'android:name=".app.TermuxActivity"' not in text:
    raise SystemExit('TermuxActivity manifest entry not found')
Path(sys.argv[1]).write_text(text)
PY

echo "DALE launcher injected into TermuxActivity."
