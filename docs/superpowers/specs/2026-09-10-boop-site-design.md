# BOOP Site Design

## Goal

Build a public static website for the BOOP project that can be deployed by unzipping files directly into a normal web root. The first live home will be `pnalgn.xyz`; the design and copy should be ready to move to `boop.com` later without architectural changes.

## Scope

This first release is a single-page public site plus a small privacy page. It introduces BOOP, explains the current working Android/TV/Wall prototype family, tells the local-first Home Assistant story, shows the protected BOOP artwork, explains the `Works FOR BOOP` idea, and clearly separates current prototypes from future product concepts.

It does not add accounts, analytics, cookies, forms, databases, ecommerce, CMS, tracking, server code, app downloads, or a build pipeline.

## Architecture

The deployable site is plain HTML, CSS and JavaScript with no framework and no runtime dependencies. Everything needed to render the site ships inside the zip. The zip root contains the deployable files directly so the user can extract/upload them into the hosting document root without moving a nested project folder.

Repository source lives on dedicated branch `boop-site` under `site/`. Website work must not alter the canonical BOOP application package, signer, permissions, Android source, launcher, Shield code, or protected app assets.

## Source authority

Website product claims and copy are derived from current BOOP authority documents and durable project memory. Current engineering facts come from live `main` and `boop-unified`; stale historical notes do not override current branch state.

Protected art is reused exactly from the repository. The site must not regenerate, redraw, recolour, crop, recompress, flatten or rewrite BOOP's approved eye master or locked yellow-hand artwork.

Canonical eye master source:

`unified/assets/boop-eyes/boopApprovedEyes.png`

Canonical locked notification-hand source:

`unified/assets/boop-notifications/boop-yellow-hands-approved.png`

The existing BOOP headphones art may also be reused from the current Shield source as a media/body illustration.

## Public story

The opening statement is:

**BOOP works FOR you.**

The site explains BOOP as an honest puppet/interface rather than a claim of sentience. It presents Home Assistant as the local smart-home authority, with basic home control not dependent on cloud conversation. It preserves the product idea that one BOOP may inhabit different useful bodies.

The page should communicate these themes in plain English:

- BOOP is useful machinery presented through expressive puppetry.
- BOOP works for the owner rather than demanding a proprietary device ecosystem.
- Local Home Assistant control comes first.
- Phone, tablet/Wall and TV/Shield are current proving grounds/bodies.
- Accessibility, repairability, visible privacy cues and open compatibility are product values.
- `Works FOR BOOP` is a compatibility idea, not a claim of partnerships with named brands.
- Future hardware concepts remain concepts until they actually exist.

## Page structure

### 1. Hero

Large BOOP eye presentation using the exact approved eye master. Copy: `BOOP works FOR you.` Supporting text introduces a local-first smart-home/media puppet without technical jargon. The eyes may be scaled, positioned, clipped or animated non-destructively in the browser.

### 2. Meet BOOP

Short explanation that BOOP is a puppet interface, not a fake person. Introduce the principle that charm is allowed while the mechanism remains honest.

### 3. One BOOP, different bodies

Show three current prototype/body groups:

- Wall / tablet
- Phone
- TV / Nvidia Shield

These are labelled as current prototypes rather than finished retail products. The section should use the same approved BOOP face/artwork rather than inventing alternate character designs.

### 4. What BOOP does

Describe currently demonstrated areas at a high level: local smart-home control through Home Assistant, media interaction, voice, notifications, room-aware control, and different device bodies. Avoid claiming physically unaccepted experimental behavior as finished.

### 5. Works FOR BOOP

Present `Works FOR BOOP` prominently. Explain that BOOP is intended to work with ordinary devices exposed through Home Assistant rather than forcing another proprietary smart-device network. Do not display third-party logos or imply formal commercial partnerships.

### 6. Privacy and visible strings

Explain the current local-first architecture and longer-term physical privacy direction in simple language. Current software facts may include local Home Assistant authority and local/offline natural voice capability after its model pack is installed. Future physical privacy ideas must be labelled as design direction rather than shipping hardware.

### 7. Accessibility and human-world design

Present big readable UI, remote-friendly interaction, sign-language/accessibility ambitions, yellow hands and the broader principle that accessibility is ordinary product quality rather than a separate lesser mode.

### 8. Future bodies

Show clearly labelled concepts such as BOOP Wall, BOOP Speaker, BOOP Robot and open accessories. Concepts must not look like products for sale. No invented technical specifications are added solely for marketing.

### 9. Workshop / open development

Explain that BOOP is actively prototyped and tested on real hardware. Link to the public GitHub repository. Do not expose an automatic latest-APK button in this first site because current CI-green candidates can still be pending physical acceptance.

### 10. Footer

Minimal footer with project/source link and plain privacy link. No newsletter, account creation, cookie banner or tracking UI.

## Visual direction

The visual system is intentionally simple and BOOP-led rather than generic SaaS design:

- white or near-white canvas with strong black typography and borders;
- cyan/blue accents derived from BOOP's default eye colour;
- yellow accent from approved hands;
- large typography and chunky interaction targets;
- asymmetric playful composition where it helps the puppet feel present;
- no stock photography;
- no AI-regenerated BOOP artwork;
- no dense dashboard aesthetic;
- no dependency on touch-only interaction.

The page must remain usable with JavaScript disabled. JavaScript adds only optional presentation behavior such as gentle eye movement, blink masking, nav state and reveal motion.

`prefers-reduced-motion` must disable non-essential movement.

## Asset handling

The deploy zip contains local copies of required public artwork so the live website does not depend on GitHub raw URLs. Asset bytes are copied from their repository blobs unchanged. Verification checks exact protected hashes before packaging.

The site may animate assets using CSS transforms, clipping and masks. It must not rewrite protected asset files.

## Hosting and deployment

Target is conventional shared hosting such as Namecheap/cPanel/Apache. Deployment must require only:

1. unzip package;
2. upload/extract contents into web root;
3. visit domain.

No Node, Python, PHP, database, shell access or post-deploy build command is required.

A conservative `.htaccess` may set `DirectoryIndex`, an HTML 404 page, basic security headers and safe cache rules, but it must not block existing sibling folders such as `/apk/` or other static download directories.

## Files

Repository lane:

- `site/index.html` — main public page
- `site/privacy.html` — short privacy explanation
- `site/styles.css` — complete responsive visual system
- `site/site.js` — optional progressive enhancement only
- `site/404.html` — lightweight missing-page response
- `site/.htaccess` — conservative shared-host configuration
- `site/assets/boop-eyes.png` — exact approved eye master bytes
- `site/assets/boop-yellow-hands.png` — exact approved hand bytes
- `site/assets/boop-headphones.png` — existing BOOP media art bytes
- `site/README-FIRST.txt` — root-upload instructions
- `site/tests/test_site.py` — static deployment/content/asset checks

Deployable zip contains the runtime files at zip root and excludes repository-only tests and planning documents.

## Testing

Automated tests verify:

- expected deploy files exist;
- protected eye and hand files match their canonical SHA-256 hashes;
- main page uses local asset paths rather than GitHub/raw CDN dependencies;
- site has no analytics/tracking scripts or external framework/font dependencies;
- essential navigation anchors resolve to real section IDs;
- concept labels are present for future hardware;
- no automatic APK/latest-release download link appears;
- JavaScript is optional and the main content remains present in HTML;
- reduced-motion handling exists;
- zip root layout is directly deployable and contains no extra enclosing folder.

Manual review remains required for appearance. Automated tests must not claim visual acceptance of BOOP artwork, geometry, animation or layout.

## Acceptance criteria

The result is accepted for this task when:

1. `boop-site` contains the site source without modifying BOOP app code.
2. Static verification passes with zero failures.
3. Protected artwork hashes match the canonical repository identities.
4. A zip is produced whose root contains `index.html` and the rest of the deployable files directly.
5. The zip can be handed to the user for extraction into the hosting root.
6. The site clearly distinguishes current prototypes from future concepts.
7. No tracking, account system, backend or external runtime dependency is introduced.
