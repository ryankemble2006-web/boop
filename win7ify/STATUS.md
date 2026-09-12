# Win7ify repair status

2026-09-12. Branch boop-win7ify-v01, source under win7ify/.

v0.1 built but failed the first user Apply test with a permissions exception.
A same-value-only probe reproduced the exception on TaskbarDa; TaskbarAl passed.
The original backup was inspected and preserved. No permissions or security settings changed.

RED proof: 8103242, Actions run 34670905369. Five regression assertions failed,
while the original nine tests passed. No artifact produced by that RED run.

Repair candidate: v0.1.1. Per-setting outcomes, no-op skipping, read-back validation,
partial-restore retry, atomic validated backup, clearer always-visible action area.
Explorer refresh is now separately confirmed, limited to the current desktop shell.
GitHub build/test and local smoke verification are pending for this candidate.
Do not call it physically accepted or install/apply it automatically.
