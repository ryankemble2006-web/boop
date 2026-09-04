# BOOP Alpha 6 — ChatGPT setup

This adds general ChatGPT-style conversation to BOOP **without an OpenAI API key**. It reuses the ChatGPT provider login already stored inside the OpenCode Home Assistant add-on.

The working Alpha 5 house/media paths remain local-first. OpenCode is only asked when Home Assistant genuinely does not understand the sentence.

## 1. Turn on two OpenCode options

Home Assistant → **Settings → Add-ons → OpenCode → Configuration**

Turn on:

- **OpenCode LAN server**
- **Startup hooks**

Save and restart the OpenCode add-on.

**Do not map `4096/tcp` in the Network section.** BOOP's bridge talks to OpenCode only inside the add-on container at `127.0.0.1:4096`.

## 2. Drop in one file

Copy:

`setup/opencode/10-boop-chatgpt.sh`

to:

`/config/opencode/startup.d/10-boop-chatgpt.sh`

Then restart the OpenCode add-on once more.

The hook creates its own persistent Python environment, installs its pinned bridge dependencies, starts one Wyoming bridge on internal port `10400`, and survives future OpenCode restarts.

### Quick check

In the OpenCode terminal run:

```bash
ha-hooks list
```

You should see `10-boop-chatgpt.sh` as **ok**.

If not:

```bash
ha-hooks log 10-boop-chatgpt.sh
```

The bridge's own runtime log is:

`/data/boop-wyoming/bridge.log`

## 3. Add the Wyoming conversation agent to Home Assistant

Home Assistant → **Settings → Devices & services → Add Integration → Wyoming Protocol**

Use:

- **Host:** `f5588468-ha-opencode`
- **Port:** `10400`

No host/LAN port mapping is required; Home Assistant Core reaches the add-on over the Supervisor network.

If Home Assistant cannot resolve that hostname, stop there rather than opening ports. The installed add-on hostname may differ from the documented stable add-on service name; confirm the OpenCode add-on's internal service hostname and use that with the same port `10400`.

After the integration loads, Home Assistant should expose a conversation agent named **BOOP OpenCode**.

## 4. Install BOOP Alpha 6

Install the Alpha 6 APK over Alpha 5. BOOP keeps the existing Home Assistant connection and device identity.

## Test in this order

1. `lights on` — local Home Assistant path.
2. `pause` — Alpha 5 direct-media path.
3. `skip` — Alpha 5 direct-media path.
4. `why is the sky blue?` — BOOP OpenCode / ChatGPT fallback.
5. `explain that simpler` — should continue the same conversation.

For the reliability test, stop the OpenCode add-on and repeat a light/media command. House and direct-media control must continue working; only a general question should report that BOOP cannot reach its assistant.

## What is deliberately not exposed

- No OpenAI API key is stored in BOOP.
- OpenCode's ChatGPT credentials never leave OpenCode.
- Port `4096` remains internal.
- BOOP's general-chat turns disable every OpenCode tool, so the fallback cannot edit files, run shell commands, or control Home Assistant.
