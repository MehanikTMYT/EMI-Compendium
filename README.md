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

Next (see docs/SCOPE.md): enchantments (UniversalEnchantmentInfo, MIT),
smithing templates (SmithingTemplateViewer — JEI-only, rewrite), ore distribution.

EMI Compendium is one of the pack's Compendium mods; the goal is to fold all of them into a single pack jar later.

### Third-party notices

- EMI Professions — Copyright (c) 2024 Mrbysco — MIT License.
- EMIffect — Copyright (c) 2022-2023 Flamarine, 2023-2025 Prismwork — MIT License.
- Just Enough Breeding — Copyright (c) 2026 Christofmeg — MIT License.
