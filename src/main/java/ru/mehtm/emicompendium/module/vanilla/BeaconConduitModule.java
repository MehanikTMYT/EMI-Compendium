package ru.mehtm.emicompendium.module.vanilla;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import ru.mehtm.emicompendium.EmiCompendium;
import ru.mehtm.emicompendium.api.CompendiumModule;
import ru.mehtm.emicompendium.recipe.StaticInfoRecipe;

import java.util.List;

public final class BeaconConduitModule
    implements CompendiumModule {

    public static final EmiRecipeCategory BEACON =
        new EmiRecipeCategory(
            EmiCompendium.id("beacon"),
            EmiStack.of(Items.BEACON)
        );

    public static final EmiRecipeCategory CONDUIT =
        new EmiRecipeCategory(
            EmiCompendium.id("conduit"),
            EmiStack.of(Items.CONDUIT)
        );

    private static final EmiIngredient BEACON_BASE_BLOCKS =
        alternatives(
            Items.IRON_BLOCK,
            Items.GOLD_BLOCK,
            Items.EMERALD_BLOCK,
            Items.DIAMOND_BLOCK,
            Items.NETHERITE_BLOCK
        );

    private static final EmiIngredient BEACON_PAYMENTS =
        alternatives(
            Items.IRON_INGOT,
            Items.GOLD_INGOT,
            Items.EMERALD,
            Items.DIAMOND,
            Items.NETHERITE_INGOT
        );

    private static final EmiIngredient CONDUIT_FRAME_BLOCKS =
        alternatives(
            Items.PRISMARINE,
            Items.PRISMARINE_BRICKS,
            Items.DARK_PRISMARINE,
            Items.SEA_LANTERN
        );

    @Override
    public String id() {
        return "beacons_conduits";
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(BEACON);
        registry.addCategory(CONDUIT);

        registry.addWorkstation(
            BEACON,
            EmiStack.of(Items.BEACON)
        );

        registry.addWorkstation(
            CONDUIT,
            EmiStack.of(Items.CONDUIT)
        );

        registry.addRecipe(
            new StaticInfoRecipe(
                BEACON,
                EmiCompendium.id(
                    "/beacon/base_blocks"
                ),
                BEACON_BASE_BLOCKS,
                EmiStack.of(Items.BEACON),
                translate(
                    "recipe.beacon.base.title"
                ),
                translate(
                    "recipe.beacon.base.tooltip"
                ),
                translate(
                    "recipe.beacon.base.line_1"
                ),
                translate(
                    "recipe.beacon.base.line_2"
                )
            )
        );

        registry.addRecipe(
            new StaticInfoRecipe(
                BEACON,
                EmiCompendium.id(
                    "/beacon/payment_items"
                ),
                BEACON_PAYMENTS,
                EmiStack.of(Items.BEACON),
                translate(
                    "recipe.beacon.payment.title"
                ),
                translate(
                    "recipe.beacon.payment.tooltip"
                ),
                translate(
                    "recipe.beacon.payment.line_1"
                ),
                translate(
                    "recipe.beacon.payment.line_2"
                )
            )
        );

        registry.addRecipe(
            new StaticInfoRecipe(
                CONDUIT,
                EmiCompendium.id(
                    "/conduit/frame_blocks"
                ),
                CONDUIT_FRAME_BLOCKS,
                EmiStack.of(Items.CONDUIT),
                translate(
                    "recipe.conduit.frame.title"
                ),
                translate(
                    "recipe.conduit.frame.tooltip"
                ),
                translate(
                    "recipe.conduit.frame.line_1"
                ),
                translate(
                    "recipe.conduit.frame.line_2"
                )
            )
        );
    }

    private static EmiIngredient alternatives(
        net.minecraft.world.level.ItemLike... items
    ) {
        return EmiIngredient.of(
            List.of(items).stream()
                .map(EmiStack::of)
                .toList()
        );
    }

    private static Component translate(String path) {
        return Component.translatable(
            "emi_compendium." + path
        );
    }
}
