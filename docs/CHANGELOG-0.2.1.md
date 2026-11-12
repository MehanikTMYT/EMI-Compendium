# 0.2.1 — Minecraft 26.1.2 / NeoForge / Java 25

## Smithing Template Viewer (Stage 1)

Adds the `smithing` module: an interactive smithing trim preview inside EMI.

- Category `emi_compendium:smithing` on the smithing table, one recipe per
  `SmithingTrimRecipe`.
- Preview shows the armor stand, the selected armour piece per slot (head,
  chest, legs, feet) and the trim material; arrows switch armour pieces and
  the trim material, the same controls as the upstream viewer.
- Armour of other mods is included automatically: base items come from the
  recipe ingredient filtered by the `EQUIPPABLE` component, which is what
  26.1.2 uses for equipment slots.
- Config `display_materials` (default `true`) hides the trim material input
  behind a "Use any item!" barrier; registered as a COMMON config of this mod
  and editable through the NeoForge config screen.
- No JEI/REI dependency of any kind; EMI stays the only recipe viewer.

Origin: the EMI plugin of SmithingTemplateViewer by Buuz135 (MIT),
https://github.com/Buuz135/SmithingTemplateViewer. The official 26.1.2 release
excludes that plugin because no NeoForge 26.1.2 EMI API was published at the
time, so this module is what makes the viewer available to EMI users. The
original MIT notice ships as `LICENSE-smithingtemplateviewer`.

API adaptations made while porting to EMI 1.1.24 / Minecraft 26.1.2:

| Upstream (1.21 / excluded EMI sources) | 26.1.2 in this jar |
|---|---|
| `ResourceLocation` | `Identifier` |
| `EmiRegistry.getRecipeManager().getAllRecipesFor(...)` | `EmiRegistry.getRecipeMap().byType(RecipeType.SMITHING)` |
| `RecipeHolder.id()` → `ResourceLocation` | `RecipeHolder.id().identifier()` |
| `GuiGraphics.drawString/blit/renderItem` | `GuiGraphicsExtractor.text/blit(RenderPipelines.GUI_TEXTURED, …)/item` |
| `InventoryScreen.renderEntityInInventory(...)` | `InventoryScreen.renderEntityInInventoryFollowsAngle(...)` with the panel clip used by the official 26.1.2 build |
| `SmithingTemplateItem.upgradeDescription` (field removed) | `Component.translatable("item.minecraft.smithing_template.upgrade")` |
| `Ingredient.getItems()` / `Ingredient.of(ItemStack)` | `Ingredient.items()` / `EmiStack.of(ItemStack)` |
| private `SmithingTrimRecipe.template/base/addition` (access transformer) | public accessors `templateIngredient()` / `baseIngredient()` / `additionIngredient()` — no access transformer needed |

Duplicate protection: if an installed Smithing Template Viewer build contains
its own EMI plugin (`…/emi/STVEmiPlugin.class`), the module registers nothing,
so only one trim viewer category exists. The official 26.1.2 release has no
EMI plugin, so the viewer keeps working next to it.

## Existing modules

No functional change. Professions, breeding (bundled Just Enough Breeding),
enchantments (bundled Universal Enchantment Info), mob effects,
beacon/conduit, Advanced Loot Info family and EMIffect keep their categories.

## Checks

See `QA_REPORT.md` for the server smoke, the client QA of all five function
groups, the `/reload` and favorites checks, the coexistence runs with the
official Smithing Template Viewer and the artifact audits.
