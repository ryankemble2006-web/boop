# Shield256 lyrics action layout

Ryan accepted255's fast lookup/editor and requested Lookup beneath play/pause, always available even with incorrect loaded lyrics. Lookup now centres beneath play/pause; Queue sits beside it to the left. Down from play/pause selects Lookup. Existing editor navigation and playback geometry remain intact.

The physical Shield screenshot showed Lookup using visibly lighter charcoal than transport. Both text actions now use the transport's exact normal/focused fills and the configured accent outline, bypassing shared Home button chrome. The transport itself is unchanged. Ryan describes the existing controls as black; do not reinterpret this as a request for a blue theme. A temporary pure-black commit was superseded by exact matching after screenshot inspection.

23 focused checks and the real emulator remote editing/search/validation/Back test passed for the final production geometry and colour values. CI found a second old Queue-coordinate assertion; it was updated to the requested location. Final signed CI36354379412 builds source d4d4cab4. Signed CI passed; Shield256 is installed. APK SHA256 6a5950841fe86ef42330336f10cf9a5dfe3462dc01f4315cbab16f024517317c matches the installed artifact. Permanent signer,29 assets,16 native libraries, all saved preferences, UID and install history are preserved. No player was launched or playback control operated. Ryan subsequently confirmed the lyrics button position is perfect.

Shield255 was accepted physically. The phone-remote IME caused its mobile-device prompt; switching the physical Shield default IME to the installed Leanback TV keyboard resolved it and Ryan accepted the result. No app code change was needed for that keyboard issue.
