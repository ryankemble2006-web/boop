#!/usr/bin/env python3
from pathlib import Path
import runpy

OLD = '''    private void emit() { listener.onViewState(new ViewState(status, cards, status == Status.LIVE && !cards.isEmpty() && !toggleInFlight, message, this::toggleCard)); }
'''
NEW = '''    private void emit() {
        ViewState state = new ViewState(
                status,
                cards,
                status == Status.LIVE && !cards.isEmpty() && !toggleInFlight,
                message,
                this::toggleCard);
        HomeDashboardStateBus.publish(room, state);
        listener.onViewState(state);
    }
'''
paths = [
    Path('shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeDashboardController.java'),
    Path('boop-build/BOOP-Alpha1/shield-lib/src/main/java/com/boop/shieldoverlay/HomeDashboardController.java'),
]
patched = 0
for controller in paths:
    if not controller.exists():
        continue
    text = controller.read_text(encoding='utf-8')
    if NEW not in text:
        if text.count(OLD) != 1:
            raise SystemExit(f'{controller}: expected one current dashboard emit method')
        controller.write_text(text.replace(OLD, NEW, 1), encoding='utf-8')
    patched += 1
if patched == 0:
    raise SystemExit('No Shield dashboard source was available to patch')
runpy.run_path('scripts/patch-unified-room-controls.py', run_name='__main__')
print(f'Shield room-scoped dashboard integration patched in {patched} tree(s)')
# The legacy bitmap-era hue cache belongs only to the early Wall baseline pass.
# Never reapply it from this late Shield dashboard pass because that would
# overwrite the procedural-eye hue setter installed later in materialization.
settings = Path('boop-build/BOOP-Alpha1/shield-lib/src/main/java/com/boop/shieldoverlay/TvSettingsView.java')
if not settings.exists():
    runpy.run_path('scripts/patch-unified-wake-arm.py', run_name='__main__')
    # The permanent approved RGBA master already supplies its own transparency.
    # Never run the retired brightness/row-span alpha reconstruction on it.
else:
    runpy.run_path('scripts/patch-unified-room-switch.py', run_name='__main__')
    runpy.run_path('scripts/patch-unified-ha-null-names.py', run_name='__main__')
    runpy.run_path('scripts/patch-unified-assistant-button.py', run_name='__main__')
    runpy.run_path('scripts/patch-unified-assistant-eligibility.py', run_name='__main__')
