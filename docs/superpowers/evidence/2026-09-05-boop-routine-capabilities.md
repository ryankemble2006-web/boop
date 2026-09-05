# BOOP Routine capability gate

The controller completed the live, read-only inspection in the authenticated
OpenCode Home Assistant add-on and supplied these results on 2026-09-06.
No credentials, configuration contents, or sensitive files were viewed or
recorded. No Home Assistant state or configuration was changed.

## Observed capabilities

- OpenCode version: `1.18.25` from `opencode --version`.
- `ha-mcp tools`: MCP server v2.8.0, full profile. It advertises bare read names
  `get_states`, `search_entities`, `get_entity_details`, and `get_home_context`.
  It also advertises mutation tools including `call_service`, `write_config_safe`,
  and `hab_run`; their presence does not authorize their use.
- `curl -fsS http://127.0.0.1:4096/experimental/tool/ids`: returned only
  `invalid`, `question`, `bash`, `read`, `glob`, `grep`, `edit`, `write`, `task`,
  `webfetch`, `todowrite`, `websearch`, `skill`, and `apply_patch`.
  The authoritative OpenCode endpoint did not return
  `homeassistant_get_states`, `homeassistant_search_entities`,
  `homeassistant_get_entity_details`, or `homeassistant_get_home_context`.
- Skill: home-assistant-configuration. The file check
  `test -f /data/.config/opencode/skills/home-assistant-configuration/SKILL.md`
  exited 0. Only existence was checked; its contents were not inspected.
- `command -v hab`: `hab`.
- `hab automation --help`: lists `create`, `get`, and `list`, alongside other
  mutation commands that must not be exposed to the model.
- hab automation create: present; `hab automation create --help` supports
  `-f, --file` path input. No automation was created.
- hab automation list: present; `hab automation list` completed with exit 0.
  Its contents were suppressed and not recorded.
- hab automation get: present; `hab automation get --help` supports retrieval
  by automation id. No automation contents were recorded.

## Exact selection and decision

Exact authoring read tool IDs: the following is the entire sorted, unique set
of reviewed candidate IDs actually proven by the OpenCode endpoint. It is an
incomplete capability set, not authorization to continue or enable tools.

```json
[
  "skill"
]
```

The MCP advertisement is not proof that OpenCode exposes its Home Assistant
read tools. No prefixed IDs have been inferred from the bare MCP names. Exact
read-only entity and automation inspection through the required OpenCode
boundary is therefore unproven. The installed skill and available bounded
`hab automation create/list/get` commands cannot compensate for that absence.

Ordinary conversation must keep every discovered tool disabled. No shell,
edit, write, service-call, update, firmware, dashboard, Zigbee, `hab_run`, or
other mutation tool may be granted to OpenCode. Future selection must use
exact ID equality and must pass this gate before authoring work continues.
No tool permissions were changed during this inspection.

Decision: STOPPED — UNSAFE CAPABILITY BOUNDARY
