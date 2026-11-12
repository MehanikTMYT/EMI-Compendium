package ru.mehtm.emicompendium.module.smithing;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.neoforged.fml.ModList;
import ru.mehtm.emicompendium.EmiCompendium;
import ru.mehtm.emicompendium.api.CompendiumModule;

/**
 * Smithing trim preview inside EMI.
 *
 * Origin: SmithingTemplateViewer by Buuz135 (MIT),
 * https://github.com/Buuz135/SmithingTemplateViewer — the EMI plugin of the
 * official 26.1.2 release is not published (the release ships JEI only), so the
 * viewer is provided by this module instead. No JEI/REI dependency.
 *
 * When a Smithing Template Viewer build that ships its own EMI plugin is
 * installed, this module steps aside so only one viewer category exists.
 */
public final class SmithingViewerModule implements CompendiumModule {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
        EmiCompendium.id("smithing"),
        EmiStack.of(Items.SMITHING_TABLE)
    );

    private static final String STV_MOD_ID = "smithingtemplateviewer";
    private static final String STV_EMI_PLUGIN =
        "com/buuz135/smithingtemplateviewer/emi/STVEmiPlugin.class";

    @Override
    public String id() {
        return "smithing";
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(Items.SMITHING_TABLE));

        SmithingTrimPreview.INSTANCES.clear();
        for (RecipeHolder<SmithingRecipe> recipe : registry
            .getRecipeMap()
            .byType(RecipeType.SMITHING)) {
            if (recipe.value() instanceof SmithingTrimRecipe trim) {
                registry.addRecipe(
                    new SmithingViewerRecipe(
                        recipe.id().identifier(),
                        new SmithingTrimPreview(trim)
                    )
                );
            }
        }

        EmiCompendium.LOGGER.info(
            "Registered smithing trim preview for {} recipes",
            SmithingTrimPreview.INSTANCES.size()
        );
    }

    /**
     * True when an installed Smithing Template Viewer already registers its own
     * EMI category (checked from the mod jar, because the official 26.1.2
     * release has no EMI plugin and must not disable this module).
     */
    public static boolean providesOwnEmiPlugin() {
        return ModList.get()
            .getModContainerById(STV_MOD_ID)
            .map(container -> container.getModInfo().getOwningFile().getFile())
            .map(file -> file.getContents().containsFile(STV_EMI_PLUGIN))
            .orElse(false);
    }
}
