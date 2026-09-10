# BOOP website handoff

Updated 2026-09-10. Website work lives on dedicated branch `boop-site` and must remain isolated from normal BOOP Android development on `boop-unified` unless Ryan explicitly changes that direction.

## Current website source

Runtime source before this documentation-only handoff commit:

`5baab626273b18d170cdee1185b8784590517979`

The first deployment target is `pnalgn.xyz`. The public identity/copy is intentionally portable to `boop.com` if that domain is acquired later.

## Deployment package

User-facing package name:

`BOOP-site-root-ready.zip`

SHA-256:

`27732056dc5c45415f1d0962d14dba82de4749d7ff94f9f6ff46cc49f8f635e5`

The ZIP is deliberately root-ready. `index.html`, `.htaccess`, `styles.css`, `site.js`, pages and `assets/` live at archive root with no enclosing `site/` or project directory. Repository tests and planning documents are excluded.

The package is meant for conventional cPanel/shared hosting: upload it into the hosting document root (typically `public_html`) and extract it there. The `.htaccess` does not rewrite or block sibling download paths such as `/apk/`.

## Exact BOOP artwork

Website artwork reuses existing BOOP source blobs, not regenerated/exported copies:

- eyes: Git blob `f95375356b20297fa2b27ab8887f65d4cce5c7fd`, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`;
- yellow hands: Git blob `d47037271bf320f4f110e3f8416f59882062afac`, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`;
- headphones: Git blob `b2112ec156668cc165747d8778a8e564e268b184`, SHA-256 `c86d8fa046d1d6b6c96c15d5a6e38f3c0b89533a65946fc6d59347e7679250e9`.

Do not optimise, re-encode, crop, recolour or regenerate protected BOOP artwork merely for the website.

## Public claims boundary

The site deliberately distinguishes current prototypes from future concepts. Natural voice playback remains unresolved after physical hardware testing; ordinary Android speech is usable. The natural path is therefore described as experimental rather than finished. There is no automatic latest-APK download link because CI-green and physical acceptance are separate BOOP states.

`Works FOR BOOP` is presented as an open compatibility direction, not as a claim of partnerships with named companies.

## Runtime shape

The site is plain HTML/CSS/vanilla JavaScript with no build step, framework, CMS, backend, analytics package, external font, advertising tracker or account system. Essential content remains in HTML when JavaScript is unavailable. JavaScript only adds progressive presentation motion and respects reduced-motion preference.

## Verification receipt

Local static contract command:

`python site/tests/test_site.py -v`

Final package run with `BOOP_SITE_ZIP=/mnt/data/BOOP-site-root-ready.zip`: 9/9 PASS, zero failures and zero skips.

Protected website asset hashes were independently checked from the staged files and again from inside the final ZIP. JavaScript syntax passed `node --check`. Archive inspection confirmed top-level `index.html`, no enclosing `site/` directory, no path traversal and no repository test files.

The site contract includes the current natural-voice truth so stale copy cannot quietly describe the unresolved path as a finished feature.

Visual appearance remains Ryan's manual acceptance boundary. These checks do not certify layout, artwork appearance or animation aesthetics.
