"""Local test helper for Kodi's documented UDP EventServer protocol."""
import socket
import struct
import sys
import time

def packet(kind, payload):
    return struct.pack('!4sBBHIIHI10s', b'XBMC', 2, 0, kind, 1, 1, len(payload), 424291, b'\0'*10)+payload

with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
    target=('127.0.0.1', 9777)
    hello=b'Boop isolated game test\0'+struct.pack('!BHII',0,0,0,0)
    sock.sendto(packet(1,hello),target)
    time.sleep(.15)
    sock.sendto(packet(10,b'\x01'+sys.argv[1].encode()+b'\0'),target)
    time.sleep(.15)
    sock.sendto(packet(2,b''),target)
