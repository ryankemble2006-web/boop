#!/usr/bin/env python3
from pathlib import Path

path = Path('boop-build/BOOP-Alpha1/shield-lib/src/main/java/com/boop/shieldoverlay/HomeAssistantRepository.java')
text = path.read_text(encoding='utf-8')

def once(old, new, label):
    global text
    if text.count(old) != 1:
        raise SystemExit(f'{label}: expected exactly one anchor, found {text.count(old)}')
    text = text.replace(old, new, 1)

once('String areaId = clean(object.optString("area_id", null));',
     'String areaId = HaJsonStrings.optional(object, "area_id");',
     'device area')
once('String name = clean(object.optString("name_by_user", null));\n                if (name == null) name = clean(object.optString("name", null));',
     'String name = HaJsonStrings.optional(object, "name_by_user");\n                if (name == null) name = HaJsonStrings.optional(object, "name");',
     'device name fallback')
path.write_text(text, encoding='utf-8')
print('HA JSON nulls preserved so device registry names can fall back correctly')
