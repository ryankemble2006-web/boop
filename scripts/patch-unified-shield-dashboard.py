#!/usr/bin/env python3
from pathlib import Path

OLD = '''    private void emit() {
        listener.onViewState(new ViewState(
                status,
                favourite,
                cards,
                status == Status.LIVE && !cards.isEmpty() && !toggleInFlight,
                message,
                this::toggleCard));
    }
'''
NEW = '''    private void emit() {
        ViewState state = new ViewState(
                status,
                favourite,
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
    if NEW in text:
        patched += 1
        continue
    count = text.count(OLD)
    if count != 1:
        raise SystemExit(f'{controller}: expected one dashboard emit method, found {count}')
    controller.write_text(text.replace(OLD, NEW, 1), encoding='utf-8')
    patched += 1
if patched == 0:
    raise SystemExit('No Shield dashboard source was available to patch')
print(f'Shield room-scoped dashboard state bus patched in {patched} tree(s)')
