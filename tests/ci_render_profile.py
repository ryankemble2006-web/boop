#!/usr/bin/env python3
"""Reduce software-rendering pixels only; keep layout dp and animation time intact."""
import re
import subprocess


def adb(*args):
    return subprocess.run(['adb', *args], check=True, text=True,
                          capture_output=True, timeout=30).stdout.strip()


def half_profile(width, height, density):
    if any(value <= 0 or value % 2 for value in (width, height, density)):
        raise ValueError('Render dimensions and density must halve exactly')
    return width // 2, height // 2, density // 2


def main():
    if adb('get-serialno') != 'emulator-5554' or adb('shell', 'getprop', 'ro.kernel.qemu') != '1':
        raise RuntimeError('Render profile requires the disposable CI emulator')
    size = re.search(r'^Physical size: (\d+)x(\d+)$', adb('shell', 'wm', 'size'), re.M)
    density = re.search(r'^Physical density: (\d+)$', adb('shell', 'wm', 'density'), re.M)
    if not size or not density:
        raise RuntimeError('Physical render profile was not reported')
    original = (int(size[1]), int(size[2]), int(density[1]))
    width, height, dpi = half_profile(*original)
    adb('shell', 'wm', 'size', f'{width}x{height}')
    adb('shell', 'wm', 'density', str(dpi))
    actual_size = adb('shell', 'wm', 'size')
    actual_density = adb('shell', 'wm', 'density')
    if f'Override size: {width}x{height}' not in actual_size.splitlines() or f'Override density: {dpi}' not in actual_density.splitlines():
        raise RuntimeError('The CI render profile was not applied')
    print(f'CI_RENDER_PROFILE original={original[0]}x{original[1]}@{original[2]} '
          f'observed={width}x{height}@{dpi}; same dp/aspect, unmodified animation time')


if __name__ == '__main__':
    main()
