# Constraints (must-follow)

- Compatibility-first: public API and runtime behavior must not break unless explicitly approved.
- Small steps only: one focused change at a time.
- After every change: run ./scripts/verify.sh and fix failures before continuing.
- If behavior is unclear: write a characterization test first, then refactor.
- No "cleanup" work unless it supports a defined task in docs/agent/TASKS.md.
- Always update docs/agent/STATUS.md after each cycle with what changed + verification results.
