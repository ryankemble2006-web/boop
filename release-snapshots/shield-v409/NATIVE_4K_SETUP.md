# Native 4K on NVIDIA Shield, same visual layout

1. Set the TV output to3840x2160at the required refresh rate.
2. In NVIDIA Developer options, under HDMI, enable **Show all supported modes in custom display modes**. Accept the required Shield reboot.
3. Remove legacy window-size overrides with `adb shell wm size reset`. The final device must report physical3840x2160without a1080poverride. The3839workaround is not required with the native mode enabled.
4. Launcher size policy already maps display height to675dp: density256for1080p and512for2160p. This keeps the configured apparent sizes while drawing at native resolution. Launcher409 also rebuilds existing text/artwork controls on a live configuration change, preserving Home/Settings focus and saved preferences.
5. Verify actual SurfaceFlinger active buffers for the launcher and Kodi are3840x2160. A4KHDMI output mode or4Kscreenshot alone does not establish native UI rendering.

Verified on the Shield after reboot: native launcher and Kodi3840x2160buffers. The visible Home controls match the old1080p-upscaled layout within1–5outputpixels of rounding/font metrics. No app data or visual-size preferences were reset.

Android-wide density is separate from the launcher's own scaling. This firmware reset an ADB density override during reboot; the launcher retained its visual sizes using its own policy. Native4Kresolution persisted.
