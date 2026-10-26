# EMI Compendium 0.2.0 QA

Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25,
EMI 1.1.24-semi151a6c88+26.1.2. Work continued from FI's existing
staged/unstaged OpenCode changes, baseline 31c5433. No push or merge.

## Automated / source checks

- Clean build: see final results below; `bash scripts/build.sh`.
- Artifact checks: `bash scripts/audit-jar.sh` and `python3 scripts/audit-release.py`.
  Validate Java class version 69, migrated namespace, unique ZIP entries,
  seven distinct mod IDs, five distinct EMI entrypoints, third-party licenses,
  and identical en_us/ru_ru key sets (68).
- API inspection: Compendium has one `@EmiEntrypoint`. Its `initialize` registers
  the effect serializer and registry adapter; `register` adds the profession
  and effect categories and recipes. Professions use registered POI predicates,
  exposing job blocks as inputs. Effects expose sources as inputs and effect
  stacks as outputs. Runtime client registration remains unverified.
- Bundled EMI plugins: Compendium, ALI, AWI, JEB, UEI. No standalone
  EMIProfessions/EMIffect plugin is included. Native breeding is skipped when
  bundled JEB is loaded. Do not install the original bundled addons alongside
  this aggregate JAR: their mod IDs would collide.
- BeaconConduitModule and StaticInfoRecipe are unchanged from the baseline
  after normalizing the package rename. Two beacon recipes and one conduit
  recipe retain their IDs, inputs and outputs. Visual behavior needs client QA.
- Full original MIT notices are packaged for EMI Professions, EMIffect, JEB,
  UEI and the four Advanced Loot Info modules. Git remote is unchanged.

## Dedicated server procedure

Use an isolated directory `qa-artifacts/server` (never a production world).
Install NeoForge 26.1.2.122 there using its installer and Java 25. Place only the
release JAR and the exact EMI dependency above in `mods`. Accept the Minecraft
EULA for this test installation. Bind `127.0.0.1`, use an unused port (25596),
set view/simulation distance 2. Run `python3 scripts/smoke-server.py`.
The harness waits up to 180 seconds for `Done`, sends `stop`, and requires exit 0.
Evidence: `qa-artifacts/server-smoke.log`, installation `install.log`.
This verifies packaged server loading and shutdown, not client EMI behavior.

## Client QA — all NOT RUN

Use a fresh client profile with the versions above and only EMI + the final
Compendium JAR. Record JAR SHA256, screenshots and latest.log. Launch a world;
repeat reload and world reconnect checks. No graphical client was exercised
in this task, and no row below is a PASS.

| Check | Steps / expected result | Status |
|---|---|---|
| Registration | Inspect EMI reload log: one Compendium plugin, one registration each of professions/effects/beacons_conduits; native breeding skipped. Repeat reload: category/recipe counts do not grow. | NOT RUN |
| R/U navigation | R on an effect opens its information page; U on a potion, food or job-site block opens the corresponding source/profession page. Workstation icons open categories. Professions have no crafted output, so R on a job block still shows its crafting recipes. | NOT RUN |
| Professions | Check all 13 vanilla job professions and representative modded POI professions. Unemployed/nitwit have no job-site page. Check block alternatives, villager appearance and reopening after switching worlds. | NOT RUN |
| Shift | Left and right Shift show the profession registry ID; releasing restores translated name. Test long modded names at different GUI scales. | NOT RUN |
| Effects | Effect icons/tooltips, beneficial/harmful/neutral colors; R page, potion/arrow/stew/food sources; no-description fallback; pagination both directions and clickable source slots. | NOT RUN |
| Favorites | Favorite an effect, restart and reload; identity/quantity retained. Unknown serialized effect safely becomes empty. | NOT RUN |
| Localization | Switch en_us ↔ ru_ru, reopen existing effect pages, inspect descriptions/titles/tooltips and wrapping. Check no raw keys and no stale-language cached descriptions. | NOT RUN |
| Beacon/conduit | R beacon: base blocks + payments; R conduit: frame blocks. U iron block/ingot/prismarine reaches correct page. Effect page for conduit power includes conduit. | NOT RUN |
| extra_stacks | Eight supplied files include conduit, ominous bottle, turtle helmet and mob sources. Add resource-pack fixture below, reload resources/EMI, verify extra source appears once; remove pack and reload, source disappears. | NOT RUN |
| Invalid extra_stacks | Unknown effect ID / malformed JSON should warn and allow other files to load. Empty/invalid ingredients must not produce clickable blank sources. Test stacked packs and duplicate entries. | NOT RUN |

Resource-pack fixture (use valid 26.1.2 `pack.mcmeta`):
`assets/qa/emiffect/extra_stacks/speed.json`

```json
{"id":"minecraft:speed","stacks":["item:minecraft:diamond"]}
```

The loader retains EMIffect's `emiffect/extra_stacks` path in any namespace.
Resources from stacked packs append; it runs on EMI registration. Verify the
resource reload lifecycle in the client. Legacy pillager bad-omen data is
intentionally excluded; the current source is ominous bottle.

## Final results

- PASS: final `clean build`, exit 0, 33 seconds, six executed Gradle tasks,
  OpenJDK 25.0.4.1. Gradle reports `test NO-SOURCE`; this is not a unit-test pass.
- PASS: both artifact audit scripts; eight extra_stacks JSON files parse.
- PASS WITH OBSERVATIONS: final packaged JAR on the isolated dedicated server,
  Compendium 0.2.0 loaded, `Done`, saved all dimensions, `DONE=True EXIT=0`.
- Observations: ALICompat's bundled Farlanders fake-loot JSON fails to resolve
  absent Farlanders items; optional LootJS mixin targets are missing. A Netty
  kqueue availability probe on Linux causes a DebugFile appender error
  (`Only supported on OSX/BSD`); the server still binds and starts. Java native
  access/Unsafe warnings and ALI unemployed/nitwit trade warnings also occur.
  These do not invalidate startup/shutdown but this is not an error-free log.
- Client QA: NOT RUN. Plugin multiplicity is verified in the artifact only;
  client discovery/reload behavior still needs the checklist above.
- SHA256 release JAR:
  `361c07cedf2174edc93ae256699dca0a2a11fccc8e3f400870ffe10f373c9e3e`.
- Evidence is retained locally under `qa-artifacts/` (ignored by Git):
  `clean-build.log`, `jar-audit.log`, `server-smoke.log`, `SHA256SUMS`.
- Git remote remains `/root/home/forks/EMI-Compendium-land/emicomp.bundle`.
  No writes to EMI, MineColonies or addon source projects; no push/merge.
