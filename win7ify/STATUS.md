# Win7ify 0.2.0 candidate

The user approved bringing the real Open-Shell Windows 7-style Start menu forward,
with no further consultation. This is no longer just the settings-only 0.1.1 app.

Implemented candidate: official pinned embedded installer, StartMenu-only feature,
separate typed profile backup and ownership journal, retryable install/undo,
effective-settings export verification, menu process/window check, large diagnostic
codes and copy/save/local-log controls. Normal UAC/SmartScreen remain in charge.

RED shell contracts: run 34672711655, all 12 tests failed before implementation.
GREEN core: run 34672991011. Existing recovery suite: run 34672991043 succeeded.
GUI/real-installer integration candidate is now awaiting its own build and real
integration cycle. It is NOT yet a verified 0.2 executable or a Yoga deployment.

Yoga was offline in this session. Hosted Windows Server standalone-menu checks
are not Windows 11 Insider Explorer-hook or visual acceptance. No screenshot tests.
Keep Android apps, signing and main untouched. Full desktop Aero remains deferred.
