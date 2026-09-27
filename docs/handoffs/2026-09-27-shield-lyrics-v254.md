# Shield254: LRCLIB first with bounded transient recovery

Ryan requested repair of Home's lyrics lookup and LRCLIB as the primary service, with Deezer as fallback. Base: accepted Shield253 owner `boop-shield-seren-v252` at `b065b911120b6a4451151a6a07571a5611304aa6`. Fix branch: `boop-shield-lyrics-v254`.

Read-only physical investigation found David Essex / Gonna Make You a Star / Best Of David Essex, duration218seconds, on Shield253. LRCLIB's search endpoint returned HTTP503 directly on the Shield and HTTP200 on a repeat; matching timed candidates exist. The exact result can contain only untimed lyrics, requiring the search route. This establishes a transient service failure, not a metadata rejection.

The lookup now gives LRCLIB the first6seconds and Deezer the subsequent2.5seconds inside the existing9second outer deadline. Each LRCLIB HTTP request gets at most one retry on transport failures or HTTP500/502/503/504. Permanent HTTP errors are not retried. Cancellation and remaining time are checked before requests and while reading. Only matching timed lyrics are accepted; artist/title and3.5second duration tolerance are unchanged. A failure from either provider remains uncertain unless the other succeeds; two clean misses report no lyrics.

New executable JVM regressions exercise the production loader, both provider clients and HTTP transport with deterministic response boundaries. They failed before the fix on503/timeout recovery and provider ordering, and pass after it. Initial focused result:14tests passed, including19 resilience assertions, Home entry, Activity, queue and presentation tests. Signed CI, independent review and installed-device validation are pending. No phone deployment is planned.
