# EMI Compendium

Unified native EMI information addon for Minecraft 26.1.2
on NeoForge.

The project does not depend on JEI or JEMI.

Initial modules:

- Beacons and conduits
- Villager professions
- Breeding
- Archaeology
- Loot and world generation
- Advancements and enchantments
- Optional Mekanism integration

## Modules (2026-10-10)

| Module | Category | Origin |
|---|---|---|
| `beacons_conduits` | Beacon / Conduit information | own |
| `villager_professions` | Villager professions and job-site blocks (villager rendered with the profession) | idea and lookup from [EMI Professions](https://github.com/Mrbysco/EMIProfessions) by Mrbysco (MIT), rewritten for 26.1 |
| `mob_effects` | Status effects: index stacks, description, sources (potions, arrows, food, suspicious stew, beacon) | ported from [EMIffect](https://git.sr.ht/~flamarine/emiffect) by Flamarine / Prismwork (MIT), incl. effect descriptions (en_us, ru_ru) |
| `breeding` | Animal breeding: food, tame requirement, offspring (spawn egg / turtle egg / sniffer egg / frogspawn), parent and baby rendered; data probed from the entity registry (`Animal#isFood`, `canMate`), so modded animals are covered | idea and layout from [Just Enough Breeding](https://github.com/Christofmeg/JustEnoughBreeding) by Christofmeg (MIT), rewritten for EMI 26.1 |
| `smithing` | Smithing trim viewer: template / base / material slots, armor stand preview, per-slot armour and trim material selectors; modded armour is picked up through the `EQUIPPABLE` component | ported from the EMI plugin of [SmithingTemplateViewer](https://github.com/Buuz135/SmithingTemplateViewer) by Buuz135 (MIT), adapted to EMI 1.1.24 / Minecraft 26.1.2 |

Next (see docs/SCOPE.md): ore distribution.

EMI Compendium is one of the pack's Compendium mods; the goal is to fold all of them into a single pack jar later.

### Third-party notices

- EMI Professions — Copyright (c) 2024 Mrbysco — MIT License.
- EMIffect — Copyright (c) 2022-2023 Flamarine, 2023-2025 Prismwork — MIT License.
- Just Enough Breeding — Copyright (c) 2026 Christofmeg — MIT License.
- Smithing Template Viewer — Copyright (c) 2024-2026 Buuz135 — MIT License.

## 0.2.1 validation

See [changes](docs/CHANGELOG-0.2.1.md) and [QA_REPORT.md](QA_REPORT.md).
0.2.1 adds the smithing trim viewer (Stage 1) and records the client QA of the
professions, breeding, enchantment, effect and smithing groups (Stage 2).
The official Smithing Template Viewer 26.1.2 release ships a JEI plugin only;
when a build with its own EMI plugin is installed, this jar steps aside so no
second viewer category appears.

## 0.2.0 validation

See [changes](docs/CHANGELOG-0.2.0.md) and [QA results and client checklist](docs/QA-0.2.0.md).
Build with Java 25 using `bash scripts/build.sh`, then run
`bash scripts/audit-jar.sh` and `python3 scripts/audit-release.py`.
Client QA remains pending. The dedicated server starts and stops successfully
with the observations recorded in the QA report.
