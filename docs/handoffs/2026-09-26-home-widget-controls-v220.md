# Home widget controls, Wall v220

Ryan accepted the full-screen Wall219 widget picker, then reported that analog and digital clocks could not be resized or centred and that Bike Finder would not line up with the existing icons.

Long-press now leaves a widget selected after release. The visible lower-right handle resizes it on a separate gesture; dragging the body moves it. Centre, Remove and Done are explicit controls. Back or an empty tap exits editing. Moving a widget to the top no longer deletes it. Occupied positions give feedback and preserve the last valid bounds. Saved bounds and rendered bounds agree, and provider minimum resize dimensions are respected within the screen.

App drops snap gently to nearby existing app rows and column centres. Existing saved positions are not migrated or automatically rearranged. The phone's five columns, labels, font setting, widget IDs, notification settings, original artwork and native libraries must remain intact.

The implementation is canonical launcher source, shared into the integrated builds by materialization. Only Wall220 is intended for installation; do not replace the accepted Shield251 or Sender2.

## Verification

- The old long-press/release behaviour was reproduced without persistent controls.
- Production geometry probe checks centring, screen clamps, resize minima, overlap rejection, page isolation, and occupied/free icon snapping. 21 focused local checks pass.
- Standalone launcher Android36 build passes.
- Before the unrelated Codex authentication interruption, emulator gestures verified persistent selection, separate resize gesture, Centre, Done and Back, preserving a widget dragged to the former deletion band, and persistence after restart.
- Independent review identified and corrected a draw/minimum mismatch, hold cancellation when the frame itself owns touch events, and potential toolbar/resize-handle obstruction. Final read-only review found no remaining important issues.
- Final emulator checks pass for row/column snapping, occupied icon drops, a plain widget-area swipe, widget movement stopping at the last free position without covering icons, and a normal Clock launch outside edit mode.
- Digital-clock/tall-widget spot checks and signed CI/install receipts are pending; this is not yet a physical-device acceptance claim.

Private snapshots and emulator harnesses stay outside the public repository.
