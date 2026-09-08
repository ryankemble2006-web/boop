#!/usr/bin/env python3
"""Add transparency around the locked BOOP eye silhouettes without changing RGB art."""
from pathlib import Path
import binascii
import struct
import zlib

PNG = b'\x89PNG\r\n\x1a\n'
EYES = ((90, 600, 419, 993), (525, 600, 854, 993))
VISIBLE_THRESHOLD = 3
EXPAND = 2


def paeth(a, b, c):
    p = a + b - c
    pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
    return a if pa <= pb and pa <= pc else (b if pb <= pc else c)


def read_rgb8(path):
    data = path.read_bytes()
    if not data.startswith(PNG):
        raise SystemExit('locked eye asset is not PNG')
    pos = len(PNG)
    ihdr = None
    payload = bytearray()
    while pos < len(data):
        length = struct.unpack('>I', data[pos:pos+4])[0]
        kind = data[pos+4:pos+8]
        body = data[pos+8:pos+8+length]
        pos += 12 + length
        if kind == b'IHDR':
            ihdr = body
        elif kind == b'IDAT':
            payload.extend(body)
        elif kind == b'IEND':
            break
    if ihdr is None:
        raise SystemExit('locked eye PNG missing IHDR')
    width, height, depth, color, comp, filt, interlace = struct.unpack('>IIBBBBB', ihdr)
    if (depth, color, comp, filt, interlace) != (8, 2, 0, 0, 0):
        raise SystemExit(f'locked eye PNG format changed: depth={depth} color={color} interlace={interlace}')
    raw = zlib.decompress(bytes(payload))
    stride = width * 3
    rows = []
    offset = 0
    prior = bytearray(stride)
    for _ in range(height):
        filter_type = raw[offset]
        offset += 1
        scan = bytearray(raw[offset:offset+stride])
        offset += stride
        recon = bytearray(stride)
        for x in range(stride):
            left = recon[x-3] if x >= 3 else 0
            up = prior[x]
            up_left = prior[x-3] if x >= 3 else 0
            value = scan[x]
            if filter_type == 0:
                recon[x] = value
            elif filter_type == 1:
                recon[x] = (value + left) & 0xff
            elif filter_type == 2:
                recon[x] = (value + up) & 0xff
            elif filter_type == 3:
                recon[x] = (value + ((left + up) >> 1)) & 0xff
            elif filter_type == 4:
                recon[x] = (value + paeth(left, up, up_left)) & 0xff
            else:
                raise SystemExit(f'unsupported PNG filter {filter_type}')
        rows.append(recon)
        prior = recon
    return width, height, rows


def chunk(kind, body):
    return struct.pack('>I', len(body)) + kind + body + struct.pack('>I', binascii.crc32(kind + body) & 0xffffffff)


def make_alpha(width, height, rows):
    alpha = [bytearray(width) for _ in range(height)]
    for left, top, right, bottom in EYES:
        if not (0 <= left < right <= width and 0 <= top < bottom <= height):
            raise SystemExit('locked eye source rectangle no longer fits canonical asset')
        for y in range(top, bottom):
            row = rows[y]
            xs = []
            for x in range(left, right):
                i = x * 3
                if max(row[i], row[i+1], row[i+2]) > VISIBLE_THRESHOLD:
                    xs.append(x)
            if not xs:
                continue
            lo = max(left, min(xs) - EXPAND)
            hi = min(right - 1, max(xs) + EXPAND)
            for x in range(lo, hi + 1):
                alpha[y][x] = 255
    return alpha


def write_rgba(path, width, height, rows, alpha):
    raw = bytearray()
    for y in range(height):
        raw.append(0)
        rgb = rows[y]
        for x in range(width):
            i = x * 3
            raw.extend((rgb[i], rgb[i+1], rgb[i+2], alpha[y][x]))
    ihdr = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    path.write_bytes(PNG + chunk(b'IHDR', ihdr) + chunk(b'IDAT', zlib.compress(bytes(raw), 9)) + chunk(b'IEND', b''))


def main():
    roots = [Path('boop-build/BOOP-Alpha1/app/src/main/res')]
    assets = []
    for root in roots:
        assets.extend(root.rglob('boop_eyes.png'))
    if len(assets) != 1:
        raise SystemExit(f'expected one canonical phone eye asset, found {len(assets)}')
    source = assets[0]
    width, height, rows = read_rgb8(source)
    alpha = make_alpha(width, height, rows)
    write_rgba(source, width, height, rows, alpha)
    print('Locked eye RGB preserved; transparent silhouette alpha added to canonical PNG')


if __name__ == '__main__':
    main()
