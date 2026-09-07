#!/usr/bin/env python3
from pathlib import Path

controller = Path('shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeDashboardController.java')
text = controller.read_text(encoding='utf-8')
old = '''    private void emit() {
        listener.onViewState(new ViewState(
                status,
                favourite,
                cards,
                status == Status.LIVE && !cards.isEmpty() && !toggleInFlight,
                message,
                this::toggleCard));
    }
'''
new = '''    private void emit() {
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
count = text.count(old)
if count != 1:
    raise SystemExit(f'Shield dashboard bus patch expected one emit method, found {count}')
controller.write_text(text.replace(old, new, 1), encoding='utf-8')
print('Shield room-scoped dashboard state bus patched')
