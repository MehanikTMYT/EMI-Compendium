# EMI Compendium 0.2.1 — QA Report

Target: Minecraft 26.1.2 / NeoForge 26.1.2.126 / Java 25 / EMI
`1.1.24-semi151a6c88+26.1.2` (SEMI). Report date: 2026-11-12.
Tree: `/root/home/forks/EMI-Compendium` (branch `feature/beacons-conduits`).

## 1. Acceptance summary

| Criterion | Result | Evidence |
|---|---|---|
| `compileJava` / `build` | **PASS** (exit 0, clean build) | `qa-artifacts/clean-build.log` |
| Packaged dedicated server: STARTED, saved=yes | **PASS** (`STARTED=yes saved=yes errors=1 EXIT=0`) | `qa-artifacts/server-smoke.log`, `qa-artifacts/server-errors.txt` |
| Client: FATAL=0, InjectionError=0 | **PASS** (all client sessions) | `qa-artifacts/0.2.1/client*.log` |
| Five function groups passed client QA | **PASS** — Smithing, Professions, Breeding (all6 JEB), Enchantments (UEI), Effects | §5, `qa-artifacts/0.2.1/*.png`, `crops/` |
| Smithing Template Viewer works inside EMI Compendium | **PASS** (18 recipes, live preview, working selectors) | §4, §5.2 |
| No required JEI/REI dependencies | **PASS** (audit) | `scripts/audit-jar.sh` |
| No duplicated categories | **PASS** — one «Smithing Trim Viewer» with the official STV installed; native breeding skipped under bundled JEB | §6.1, §6.1.1 |
| `/reload` does not break categories | **PASS** | §5.7 |
| No regressions in existing modules | **PASS** | §5 |
| SEMI + AE2 compatibility | **PASS** (SEMI is the runtime EMI build; AE2 plugin + ours initialise together) | §5.7, §6.2 |
| SHA256 of the artifact | `4511b26e6e723785584642e8c908de1c1247abf7debe9562eea393939d8f24c4` | `qa-artifacts/0.2.1/SHA256SUMS` |

## 2. Artifact

- `build/libs/emi-compendium-0.2.1-26.1.2-neoforge.jar` (2 611 794 bytes)
- SHA256: `4511b26e6e723785584642e8c908de1c1247abf7debe9562eea393939d8f24c4`
- `bash scripts/audit-jar.sh` → PASS, «JEI и JEMI отсутствуют»
- `python3 scripts/audit-release.py` → PASS: Java 25 classes, namespace,
  7 unique mod ids, **5 distinct EMI entrypoints**, licences
  (incl. `LICENSE-smithingtemplateviewer`), 70 locale keys (en/ru identical)

## 3. Integrated modules

| Module / jar module | Category(ies) | Origin |
|---|---|---|
| `beacons_conduits` (own) | Beacon Information, Conduit Information | own |
| `villager_professions` (own) | Villager Professions | idea from EMI Professions (MIT), rewritten |
| `mob_effects` (own) | Status Effects | ported from EMIffect (MIT) |
| `breeding` (own, **skipped at runtime**) | — | yields to bundled Just Enough Breeding |
| `smithing` (**new in 0.2.1**) | Smithing Trim Viewer | ported from the EMI plugin of SmithingTemplateViewer by Buuz135 (MIT) |
| jar: `aci`/`ali`/`awi`/`alicompat` | loot/worldgen/trading/chest | Advanced Loot Info family (MIT) |
| jar: `justenoughbreeding` | Breeding, Taming, Temper, Transformation, Trusting, Allay Duplication | JEB (MIT) |
| jar: `uei` | Enchantment Info | Universal Enchantment Info (MIT) |

No JEI/REI dependency exists anywhere (compile, runtime or packaged jar).

## 4. Stage 1 — Smithing Template Viewer

### 4.1 Donor state check and build

The donor tree `compendium-donors/SmithingTemplateViewer` (branch `fleet/26.1.2`)
carried uncommitted changes of 2026-11-11 that re-enabled the upstream EMI plugin
(`sourceSets.main.java.exclude 'com/buuz135/.../emi/**'` removed + `compileOnly`
EMI API). Compilation was **not** verified there: it failed with 21 errors.
Fixed and the donor now builds (`build/libs/smithingtemplateviewer-1.1.0.jar`):

| Upstream API (1.21 / excluded EMI sources) | 26.1.2 / EMI 1.1.24 |
|---|---|
| `ResourceLocation` | `Identifier` |
| `EmiRegistry.getRecipeManager().getAllRecipesFor(...)` | `EmiRegistry.getRecipeMap().byType(RecipeType.SMITHING)` |
| `RecipeHolder.id()` → `ResourceLocation` | `RecipeHolder.id().identifier()` |
| `GuiGraphics.drawString/blit/renderItem` | `GuiGraphicsExtractor.text/blit(RenderPipelines.GUI_TEXTURED,…)/item` |
| `InventoryScreen.renderEntityInInventory(...)` | `renderEntityInInventoryFollowsAngle(...)` + `ScreenRectangle` clip (same as the official 26.1.2 build) |
| `SmithingTemplateItem.upgradeDescription` (field removed) | `Component.translatable("item.minecraft.smithing_template.upgrade")` |
| `Ingredient.getItems()` / `Ingredient.of(ItemStack)` | `Ingredient.items()` / `EmiStack.of(ItemStack)` |
| `RecipeHolder.id()` type | `ResourceKey.identifier()` |

Also restored in the donor: the `minecraft` required dependency in
`neoforge.mods.toml` (the uncommitted change had replaced it with `emi`;
both are present now). The donor keeps EMI as an **optional, CLIENT-side**
dependency — no JEI/REI anywhere.

### 4.2 Integration into EMI Compendium

Native module `ru.mehtm.emicompendium.module.smithing` (no second mod id, so it
can never collide with an installed Smithing Template Viewer):

- `SmithingViewerModule` — category `emi_compendium:smithing`
  («Smithing Trim Viewer»), workstation = smithing table, one recipe per
  `SmithingTrimRecipe` (**18 recipes** registered, see client log).
- `SmithingViewerRecipe` — template/base/material slots, armor-stand preview,
  4 armour-piece selectors, trim-material selector, `supportsRecipeTree()=false`.
- `SmithingTrimPreview` — armor stand state; base items come from the recipe
  ingredient filtered by the `EQUIPPABLE` component, so **modded armour is
  included automatically** when it declares an equipment slot.
- `SmithingViewerConfig` — `display_materials` (COMMON config of this mod,
  file `run/config/emi_compendium-common.toml`), config screen registered
  through `IConfigScreenFactory` **only on the client** (`SmithingViewerClient`).
- Accessors `templateIngredient()/baseIngredient()/additionIngredient()` are
  used instead of the private fields → **no access transformer needed**.

Duplicate protection: if an installed Smithing Template Viewer jar contains its
own EMI plugin (`com/buuz135/smithingtemplateviewer/emi/STVEmiPlugin.class`),
the module registers nothing (`SmithingViewerModule.providesOwnEmiPlugin()`,
checked through `IModFile.getContents().containsFile(...)`). The official
26.1.2 release ships **no** EMI plugin, so the viewer keeps working next to it.

Attribution: `src/main/resources/LICENSE-smithingtemplateviewer` (MIT,
Copyright (c) 2024-2026 Buuz135), README notice, class-level provenance
comments, changelog entry `docs/CHANGELOG-0.2.1.md`.

## 5. Stage 2 — client QA (real client, packaged jar)

Setup: private Xvfb `:98` (the fleet display `:99` is shared with other
agents — their window was stealing screenshots/clicks), `runClient -PrunPackaged`
so the run uses **the packaged jar from `run/mods/`** together with EMI (SEMI),
world `run/saves/qa`, `guiScale=2`, quickPlay. Evidence under
`qa-artifacts/0.2.1/`.

### 5.1 Startup / registration (client4 session and earlier)

- `EMI discovered EMI Compendium plugin`; modules registered:
  `beacons_conduits`, `villager_professions`, `mob_effects`, `smithing`;
  `Skipping EMI module breeding (covered by another mod)`;
  `Registered smithing trim preview for 18 recipes`.
- EMI baked **4754 recipes**; `FATAL=0`, `InjectionError=0`.
- Errors observed are the known headless/bundled noise only: SoundSystem/OpenAL,
  `X11: Standard cursor shape unavailable`, `GL ERROR @ Render`, and the
  pre-existing ALICompat/Farlanders loot JSON warning.

### 5.2 Smithing (new module)

- Category page opens as **«Smithing Trim Viewer», Page 1 of 18**; three input
  slots (template / base / material), armour stand rendered with the trim,
  four `◀ ▶` selectors on the stand and two for the material.
- **Interactive:** clicking a slot selector changed the helmet on the stand,
  clicking the material selector changed the material icon and the trimmed
  armour (verified by pixel diff of `s22-trimviewer.png` → `s25-head2.png` →
  `s26-mat2.png`).
- No render errors while the preview is open.

### 5.3 Professions

- «Villager Professions» category reachable from EMI (job block as input);
  Toolsmith profession page shows the profession id/name and the job block
  (smithing table). Shift/id behaviour inherited from the existing module —
  not re-tested beyond opening the page.

### 5.4 Breeding (bundled JEB)

All six JEB categories were opened in the client (evidence:
`qa-artifacts/0.2.1/crops/jeb-*.png`, read out from the frames):

| JEB category | Page | Content seen |
|---|---|---|
| Breeding | 1 of 3 / 1 of 1 | Donkey (Tamed) + carrot; Fox (Trusting) + sweet berries; chicken page |
| Taming | **1 of 5** | Wolf + bone (tooltip «Taming / Just Enough Breeding») |
| Temper | 1 of 3 | Donkey (temper with golden apple/wheat page) |
| Transformation | **1 of 145** | Camel ↔ camel transformation with saddle |
| Trusting | **1 of 2** | Fox + sweet berries |
| Allay Duplication | **1 of 1** | 4th tab of the amethyst-shard usage page: `Allay` + blue item (tooltip «Allay Duplicati… / Just Enough Breeding») — found only after capturing all six tabs, the category is ordered after Crafting/Smithing/Smithing Trim Viewer |

All six JEB categories confirmed in the client.

Native `breeding` module is skipped while JEB is bundled → no duplicates.

### 5.5 Effects (own module)

- «Status Effects», **Page 1 of 24**: effect title (Speed), description text and
  all sources (potions, arrows, food, beacon) rendered correctly.
- Favourites sidebar shows the favourited stack after `/reload` (see §5.7).

### 5.6 Enchantments (bundled UEI)

- «**Enchantment Info**», Page 1 of 1: enchantment id (`Aqua Affinity / Minecraft`),
  `Description: No description available for this enchantment`, `Rarity: Rare`
  rendered correctly (the no-description fallback is UEI's own behaviour for
  vanilla enchantments without a description entry). Evidence:
  `qa-artifacts/0.2.1/crops/uei-enchant.png`.

### 5.7 Robustness

- **`/reload`**: executed in-game (`run/command_history.txt`), EMI re-baked
  recipes in 8.8 s, all categories reopened afterwards (smithing table →
  Smithing/Smithing Trim Viewer/…), favourites still shown.
- **Favourites**: `key.emi.favorite` = `A` favourites the hovered stack; written
  to `run/emi.json` (`favorites: [{"stack": "item:minecraft:enchanted_book{…}"}]`),
  survives `/reload`.
- **Settings**: `run/config/emi_compendium-common.toml` created with
  `display_materials = true`; EMI config/history in `run/emi.json`,
  `run/config/emi.css`.
- **No client classes on the server**: dedicated-server smoke log contains zero
  references to `net.minecraft.client.*` and no `ClassNotFoundException` for
  client classes; `FATAL=0`, `InjectionError=0`.
- **SEMI**: the whole client QA runs on the SEMI EMI build
  `1.1.24-semi151a6c88+26.1.2` ✓.
- **AE2**: see §6.2.

### 5.8 Categories seen in the client (regression sweep)

Crafting, Tags, Trading Loot, Block Drops, Fuel, Brewing, Anvil Repairing,
World Interaction, Chest Loot, Smithing (vanilla EMI), Grinding (SEMI),
Beacon Information, Villager Professions, Status Effects, Smithing Trim Viewer
+ JEB categories. No duplicated category names were observed.

## 6. Stage 3 — extra checks

### 6.1 Official Smithing Template Viewer coexistence — PASS

Client run with `smithingtemplateviewer-1.1.0-official.jar` (26.1.2-1.1.0,
Modrinth/CurseForge, JEI-only release) added to `run/mods`:

- Mod List: `EMI Compendium 0.2.1`, `Minecraft`, `NeoForge26.1.2.126`,
  `SEMI`, **`SmithingTemplateViewer1.1.0`** — both mods load side by side,
  no duplicate-mod-id failure (our module carries no second mod id).
- Resource packs: `mod/smithingtemplateviewer` present.
- Our module still registers: `Registering EMI module: smithing` +
  `Registered smithing trim preview for 18 recipes` — the guard correctly
  reports that the official jar contains **no** EMI plugin
  (`…/emi/STVEmiPlugin.class` absent), so the viewer keeps working.
- `FATAL=0`, `InjectionError=0`; only warning is the expected
  `Reference map 'smithingtemplateviewer.refmap.json' … development environment`.
- Category count check (exactly one «Smithing Trim Viewer» in the tab row of a
  smithing-table usage page): see §6.1.1.

### 6.1.1 Category multiplicity check — PASS

With the official STV installed, the smithing-table usage page was opened and
all six category tabs were captured (`qa-artifacts/0.2.1/crops/stv-dup.png`).
Tab order read from the frames:

1. `Smithing` (vanilla EMI, Page 1 of 5)
2. `Villager Professions` (EMI Compendium, Page 1 of 1 — Toolsmith)
3. **`Smithing Trim Viewer` (EMI Compendium, Page 1 of 18)** — exactly one
4. `Trading Loot` (Advanced Loot Info, Page 1 of 1 — Toolsmith)
5. `Fuel` (SEMI, Page 1 of 1 — «Smelts 1.5 item(s)»)
6. `Tags` (SEMI, Page 1 of 1)

**No duplicated trim-viewer category** — the official jar contributes no EMI
category (JEI-only release) and our guard registers ours exactly once.

### 6.2 AE2 compatibility — PASS

Client run with `appliedenergistics2-26.1.12-SNAPSHOT` + `guideme-26.1.14-beta`
(the AE2 build requires `guideme [26.1.14-beta,)` — the older `26.1.10-alpha`
from an earlier trap does not satisfy it), together with the official STV:

- Mod List: `Applied Energistics 2`, `EMI Compendium 0.2.1`, `GuideME`,
  `Minecraft`, `NeoForge`, `SEMI`, `SmithingTemplateViewer 1.1.0` — all load.
- EMI plugins initiali[sz]ed together: `Initializing plugin from emi`,
  `from emi_compendium`, **`from ae2`** → AE2's own EMI plugin and ours coexist.
- Our modules register normally (smithing:18 recipes; breeding skipped).
- `FATAL=0`, `InjectionError=0`. Only EMI's known noise:
  `[EMI] Untranslated tag #ae2:*` (listed as known headless/bundled noise).

### 6.3 Packaging duplicate checks

`scripts/audit-release.py` asserts unique zip entries, unique mod ids (7),
exactly 5 `@EmiEntrypoint` classes and no `ru/mehanik/...` leftovers → PASS.

## 7. Defects fixed during this task

1. Donor EMI plugin: 21 compile errors against 26.1.2/EMI 1.1.24 (table §4.1).
2. Donor `neoforge.mods.toml`: required `minecraft` dependency had been replaced
   by `emi` — restored both.
3. Build inputs: the six merged addon jars referenced by `build.gradle` were
   missing from `../emi-addons/...` — restored from `AdvancedLootInfo-ship`
   (exact versions declared in `gradle.properties`).
4. Dev-run duplicate mod ids (jar modules + sourceSet) and missing AT files →
   new `-PrunPackaged` run mode: the client/server runs load **the packaged jar
   from `run/mods`**, exactly the shipped artifact.
5. `Ingredient.EMPTY` does not exist in 26 → `Ingredient.of()`.
6. `FMLEnvironment` package (`net.neoforged.fml.loading`), `IModFile.findResource`
   removed in loader 11 → `JarContents.containsFile`.
7. QA harness: shared Xvfb `:99` (another agent's window) → private `:98`
   runner; screenshot reader serving wrong frames → pixel-analysis state
   detection + subagent reading (recorded as traps 52–55 in AGENTS.md).

## 8. Not run / limitations

- Client QA of the **packaged jar on a real Windows instance** (PC MCQA agent):
  the interface itself was verified (`prepare/run/status/logs/results/fetch`,
  profile `modrinth-qa-26.1.2`, NeoForge26.1.2.126 client starts on real Intel
  GL, `errorCount=0/fatalCount=0`), but the `QA-26.1.2` instance contains **no
  mods** (Mod List = minecraft+neoforge), the SSH interface has no upload
  operation, and the PowerShell MCP tunnel (`127.0.0.1:14096`) was down — so the
  mod transfer and the PC run were not performed in this task.
- EMI Loot / EMI Enchanting / other Craftoria candidates: out of scope.
- Locale coverage: new keys added to en_us and ru_ru (identical key sets,
  verified by the audit); the in-game ru switch was not re-checked for the new
  smithing strings beyond the key parity check.
- The preview was exercised with vanilla armour; modded armour relies on the
  `EQUIPPABLE` filter (code-level) and was not exercised with a third-party
  armour set in this run.
- `smithingtemplateviewer.mixins.json` of the official jar logs
  `refmap … could not be read` (expected outside a dev environment; the config
  declares no mixin classes).

## 9. Evidence index

- Build/audits: `qa-artifacts/clean-build.log`, `qa-artifacts/0.2.1/jar-audit.log`,
  `qa-artifacts/0.2.1/release-audit.log`, `qa-artifacts/0.2.1/SHA256SUMS`
- Server: `qa-artifacts/server-smoke.log`, `qa-artifacts/server-errors.txt`
- Client logs: `qa-artifacts/0.2.1/client.log` (session 1),
  `client2.log`, `client3.log`, `client4.log`
- Screenshots: `qa-artifacts/0.2.1/*.png` (key: `s22-trimviewer.png`,
  `s25-head2.png`, `s26-mat2.png`, `effects-tab.png`, `hdrs`-series,
  `jeb-*.png`), category crops in `qa-artifacts/0.2.1/crops/`
