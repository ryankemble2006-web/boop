# Shield v217 grab/reorder checkpoint

Date: 2026-09-18

Branch: `boop-shield-grab-reorder-v217`  
Owning split branch: `boop-wall-shield-split-v207`  
Shield package: `com.boop.shieldoverlay`  
Shield version: `217` / `1.2.217-shield`  
Wall remains: v207

## Requested behavior

Ryan asked for two matching remote interactions:

1. Press-and-hold on a HOME favourite must make the icon/banner visibly pop out so it is obvious the hold has engaged.
2. HA Controls must support rearranging device buttons using the same hold/reorder interaction.

## Implementation

### HOME favourites

The existing favourite grab/reorder mechanics were retained.

Ordinary focus still keeps HOME artwork at 1.00x. An active grab now changes the artwork to 1.14x and adds 10dp Z-depth. The stronger effect is therefore exclusive to the hold/grab state and does not disturb the accepted favourite-row spacing.

### HA Controls

`ShieldRoomPanelView` reuses `FavouriteGrabSession`:

- long-press enters grab mode;
- grabbed tile scales to 1.10x and receives 10dp Z-depth;
- DPAD left/right reorders the tile;
- OK/Enter commits/drops;
- DPAD up/down is consumed while grabbed.

Outside grab mode, the existing click handler remains the HA toggle path.

### Persistence

`ShieldHomeStore` stores a room-specific entity-ID order under `room_control_order_v1:<room-id>`.

Reconciliation:
- retain saved IDs that are still available;
- discard unavailable/removed IDs;
- append newly discovered IDs in their live order;
- keep rooms independent.

Changing rooms cancels a current grab.

## Verification

Verified source: `4fde718e7ee928f87438f452022dc8e38f858163`.

GitHub Actions:
- successful run: `35351389519`
- job: `105620113142`
- artifact: `10549884892`
- artifact name: `BOOP-Shield-v217-Wall-v207-Signed`
- uploaded ZIP SHA-256: `51106302b2394af6dd2fc79a47dd1599ed1e9a0015b56866ca02a30615c29aa9`

Shield APK:
- file: `BOOP-Shield-v217.apk`
- bytes: `160485741`
- SHA-256: `a6742e4fa9e3457ffe0384c9f1c41d5a1a97b4231c39b7b06688f68122227b3b`
- signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- 16 native libraries remain byte-identical to the accepted baseline.

CI passed focused checks, inherited v206 checks, split materialization/integration, HA tests, both APK builds, and signer/native/art verification.

The first v217 run `35351182952` stopped before APK build because an inherited v205 source assertion still required grabbed HOME artwork to remain at 1.00x. That assertion represented the old behavior the user explicitly asked to change. The test was updated to retain 1.00x ordinary focus while requiring the new 1.14x + Z grab state.

## Physical acceptance

Pending Ryan's Shield test:
- favourite grab clearly pops compared with ordinary focus;
- favourite left/right reorder and OK drop remain correct;
- HA tile grab visibly lifts;
- HA left/right reorder and OK drop work;
- HA order survives HOME/app restart;
- normal HA toggles retain current response behavior.

No voice/audio, weather, HA transport, permissions, signer, Wall code or daily-phone behavior was intentionally changed.
