#!/usr/bin/env python3
"""Apply small integration adapters to both preserved and materialized Shield sources."""
from pathlib import Path


def replace_once(text, old, new, label):
    if new in text:
        return text
    if text.count(old) != 1:
        raise SystemExit(f"{label}: integration anchor changed; refusing a blind patch")
    return text.replace(old, new, 1)


def replace_method(text, signature, body):
    start = text.find(signature)
    if start < 0 or text.count(signature) != 1:
        raise SystemExit(f"Expected one method: {signature}")
    opening = text.index("{", start)
    depth = 1
    end = opening + 1
    while depth and end < len(text):
        depth += (text[end] == "{") - (text[end] == "}")
        end += 1
    if depth:
        raise SystemExit(f"Unclosed method: {signature}")
    return text[:opening + 1] + "\n" + body + "\n    }" + text[end:]


roots = [
    Path("shield-overlay/app/src/main/java/com/boop/shieldoverlay"),
    Path("boop-build/BOOP-Alpha1/shield-lib/src/main/java/com/boop/shieldoverlay"),
]
for root in roots:
    if not root.is_dir():
        continue
    repository = root / "HomeAssistantRepository.java"
    text = repository.read_text(encoding="utf-8")
    text = replace_once(text,
        'JSONArray categories = registryResult.optJSONArray("entity_categories");',
        'Object categories = registryResult.opt("entity_categories");', "HA category table")
    text = replace_once(text,
        'String category = entityCategory(object.opt("ec"), categories);',
        'String category = HaEntityCategory.resolve(object.opt("ec"), categories);', "HA category decoding")
    text = replace_method(text, "private static boolean isDashboardControl(EntityCard card)",
        "        return RoomDeviceControls.isActionable(card);")
    repository.write_text(text, encoding="utf-8")

    controller = root / "HomeDashboardController.java"
    text = controller.read_text(encoding="utf-8")
    text = replace_method(text, "private static boolean isActionable(EntityCard card)",
        "        return RoomDeviceControls.isActionable(card);")
    controller.write_text(text, encoding="utf-8")

    activity = root / "BoopHomeActivity.java"
    text = activity.read_text(encoding="utf-8")
    text = replace_once(text, '''                    onContentLeft,
                    () -> {
                        if (dashboardController != null) {
                            dashboardController.toggleFavourite();
                        }
                    });''', '''                    onContentLeft,
                    this::showRoomPicker);''', "Home room selector")
    activity.write_text(text, encoding="utf-8")

    settings = root / "TvSettingsView.java"
    text = settings.read_text(encoding="utf-8")
    text = replace_method(text, "private void ensureVisible(View child)",
        "        child.post(() -> child.requestRectangleOnScreen(\n"
        "                new Rect(0, 0, child.getWidth(), child.getHeight()), true));")
    text = replace_method(text, "private void focus(boolean f)",
        "            setBackground(f ? round(CYAN, Color.WHITE, 2) : round(PANEL, Color.rgb(58,58,64), 2));\n"
        "            setPadding(dp(30), dp(20), dp(30), dp(20));\n"
        "            title.setTextColor(f ? Color.BLACK : Color.WHITE);\n"
        "            value.setTextColor(f ? Color.BLACK : CYAN);\n"
        "            detail.setTextColor(f ? Color.rgb(18,40,44) : Color.rgb(170,170,178));")
    settings.write_text(text, encoding="utf-8")

print("Shield room selector, stable control filtering and keyed HA metadata integrated")
