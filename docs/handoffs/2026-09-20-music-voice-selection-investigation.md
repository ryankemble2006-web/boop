# Music voice selection investigation, 2026-09-20

## User feedback and scope

Ryan reports v241 seems to fix the favourite confirmation: he successfully added a track from a Queen album manually selected in Deezer. This is a positive user test of that case, not exhaustive heart acceptance. He now reports spoken Queen choosing a namesake song, John Lennon Imagine choosing an unrelated literal title, John Lennon failing, and Britney Spears opening a playlist. He asks whether this needs training or another session. This continuation investigates only; no app code, device input, playback, permissions or installations were changed.

## Source and controlled evidence

Inspected live owner be0bdebbf73374b035662909ed16106de9fb5b04 on boop-shield-weather-focus-v221. The relevant catalogue, parser, native controller and music client are identical to the source of the Wall208 actually installed on Pixel7. Local task checkout was clean and matched live HEAD; the old primary checkout was not changed.

The parser puts ordinary Play requests into a generic Deezer search. DeezerCatalogue.java splits title and performer only at a literal ' by '. It does not interpret artist-first phrasing or possessives. It compares normalized exact titles; it does not strip recording/version suffixes. Artist choice depends on exact artist names, the top25 track results, and performer-versus-title counts; a first exact title and ties can favour a namesake song. Duplicate artist names are rejected unless other track evidence resolves identity. These are handwritten selection rules, not a model learning from spoken corrections.

Eight unauthenticated, read-only requests captured current artist and track results for the four user examples. A private JVM diagnostic then ran the actual unchanged DeezerCatalogue and MediaRequest sources against those captured JSON replies. The exactArtist/normal methods were extracted unchanged into a thin boundary adapter. Existing verified JSON dependency and installed Java were reused; no new dependencies were installed.

At this test time: Queen selected artist412, John Lennon selected artist226, Britney Spears selected artist483, and John Lennon Imagine returned NO_MATCH. Thus the original wrong Queen/Imagine selections and Lennon failure were NOT reproduced end-to-end. Exact audio transcripts, original result lists, chosen execution route and failure stage remain unknown. Do not claim the entire symptom is proven to have one cause or that recognition is error-free.

The live Imagine query included John Lennon's Imagine (Remastered 2010) and Imagine (Ultimate Mix), but the current code seeks the whole phrase as an exact title. Artist-only selections explicitly request Play top tracks; a list after Britney may be the intended artist queue, not proof an unrelated playlist was selected. Distinguish those cases in runtime evidence.

DeezerArtistClient returns null on unresolved non-explicit-provider requests so later routing can continue. Multiple target/discovery/native errors all become the same Failed message. Better route-specific, privacy-limited diagnostics are needed to separate catalogue ambiguity, recognition, and execution rather than mask errors.

## Proposed next bounded pass, not implemented

Improve the existing shared music resolver: artist/title/album intent and both common artist/title word orders; verify performer IDs and track identity rather than accept namesake titles; conservative version handling; ask on real ambiguity; keep the working native playback and room guards. Make these exact user examples plus collision cases deterministic regression fixtures. Capture recognized text, resolved kind and sanitized selection/failure stage during an explicitly bounded phone test. No need for an artist-by-artist spoken training list. This proposal is not a new APK or completed fix.
