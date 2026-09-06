# Routine-authoring research handoff — 2026-09-06

This is a research/design branch, not a complete routine-authoring feature.
Previously local commit1759bdf adds the capability evidence and its gate test.
The actual evidence says STOPPED — UNSAFE CAPABILITY BOUNDARY: the OpenCode
endpoint did not expose the required exact Home Assistant read-tool IDs.
Do not infer IDs, grant broad shell/write tools or alter the evidence to green.

Fresh check:123 Python tests, one FAILURE in
test_live_capabilities_are_explicit_and_safe. It expects SAFE TO CONTINUE and
an HA entity-search ID, neither of which the observed capability set supplies.
The remaining122 checks pass. This is published as blocked research to preserve
what was actually discovered, not a green release or authorization to continue.
No authoring backend or Home Assistant configuration change in this sync.

Wall's accepted runtime is tracked separately on boop-wall-resurrection.
Creation must be explicitly requested and confirmed in plain speech, including
polite yes please. Timed routines remain excluded until separately redesigned.
