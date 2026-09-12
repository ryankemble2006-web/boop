# BOOP defaults continuation memory

The requested feature is BOOP-branded: Use BOOP defaults and Undo BOOP defaults.
Work only in boop-shield-defaults, branched from accepted v135 plus its docs at
582bd0d404a3d4ca61abd718551f7af5ef20aabe. Do not disturb ongoing lyrics/animation
or integration work. The user approved implementation with go; deployment is
separate. Candidate143 is not a merge of other later-numbered app branches.

Preset v1 is frozen: 14 exact IDs / nine disables / nine boot selections / six
paired background limits. See StartupDefaultsProfile.java and the spec. Do not
promote broad NVIDIA/Android categories to dynamic disable rules. Exclude the
three older/unattributed disables and Kodi's prior single app-op setting. A new
profile revision must preserve compatibility with older Undo journals; do not
silently edit the frozen list in place.

Share requested actions, not private Restore snapshots. Each receiving Shield
records its own exact pre-preset state before writes. Group Undo must not call
global Restore blindly: doing so would erase earlier customizations. Preserve
original individual ledger bytes and untouched dimensions. Keep newer-state or
ledger conflicts, interrupted journals and the first baseline. Keep current
settings is an explicit record-only escape; it is not a device reset.

The native review is required before Apply. Active input/accessibility providers,
foreground packages and recovery components are skipped. Require NVIDIA TV,
completed setup/current user/local control and an available BOOP HOME before
changing the stock launcher last. No new grants or platform text-size changes.

Tests and Android compilation passed. This is not real-device acceptance of the
preset or its new review layout. No physical device changes have been performed.
The original accepted v135 app is the source baseline; later installed device
state belongs to concurrent user-authorized work and must be rechecked before
any deployment. Read SESSION_HANDOFF.md for current build/provenance.
