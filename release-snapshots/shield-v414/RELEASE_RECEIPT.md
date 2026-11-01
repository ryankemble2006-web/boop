# BOOP Shield v414 Release Receipt

- **Shield Version**: `1.2.414-shield` (`versionCode=414`, built on verified `BOOP-Shield-v407.apk` baseline)
- **Wall Version**: `1.2.222-wall` (`versionCode=222`)
- **Highlights**:
  1. **Custom App Banner Asset Override Pipeline (`assets/banners/<packageName>.png`)**: Added a direct asset banner override hook in `TvAppCardView.loadCustomBanner(String packageName)`. The launcher now intercepts app banners from local assets before querying Android's `PackageManager`, rendering bespoke 16:9 cards immune to third-party app updates or Play Store overwrites.
  2. **Bespoke BBC iPlayer Studio White Card**: Shipped a clean, high-resolution 16:9 ceramic white studio card for `com.nvidia.bbciplayer` (`assets/banners/com.nvidia.bbciplayer.png`) scaled up to match the bold visual weight of neighboring cards (Kodi and YouTube). Eradicates Auntie Beeb's 2:1 off-black compression halo on the Sony A95L QD-OLED display.
  3. **Inherited v413 Capabilities**: Full X1S auto-reconnect on boot/wake, self-healing Accessibility services, native 4K display geometry guard (`3839x2160 @ 640 DPI`), and dynamic active theme accent resolution.
