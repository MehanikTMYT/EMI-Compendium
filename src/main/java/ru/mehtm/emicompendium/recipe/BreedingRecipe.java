package ru.mehtm.emicompendium.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import ru.mehtm.emicompendium.EmiCompendium;

import java.util.List;

/**
 * Breeding an animal: parent rendered on the left, food and spawn egg as inputs, offspring (spawn egg or laid item)
 * as output with the baby rendered on the right. Not part of the recipe tree.
 */
public final class BreedingRecipe implements EmiRecipe {
    private static final int HEIGHT = 56;

    private final EmiRecipeCategory category;
    private final EntityType<?> type;
    private final LivingEntity parent;
    private final @Nullable LivingEntity baby;
    private final EmiIngredient food;
    private final EmiStack spawnEgg;
    private final EmiStack offspring;
    private final boolean tamed;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final int entitySize;
    private final Identifier id;

    public BreedingRecipe(
        EmiRecipeCategory category,
        Holder.Reference<EntityType<?>> type,
        LivingEntity parent,
        @Nullable LivingEntity baby,
        EmiIngredient food,
        EmiStack spawnEgg,
        EmiStack offspring,
        boolean tamed
    ) {
        this.category = category;
        this.type = type.value();
        this.parent = parent;
        this.baby = baby;
        this.food = food;
        this.spawnEgg = spawnEgg;
        this.offspring = offspring;
        this.tamed = tamed;
        this.inputs = spawnEgg.isEmpty() ? List.of(food) : List.of(food, spawnEgg);
        this.outputs = offspring.isEmpty() ? List.of() : List.of(offspring);
        float extent = Math.max(parent.getBbHeight(), parent.getBbWidth());
        this.entitySize = Math.clamp((int) (32 / Math.max(extent, 0.1F)), 4, 24);
        Identifier key = type.key().identifier();
        this.id = EmiCompendium.id("/breeding/" + key.getNamespace() + "/" + key.getPath());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 156;
    }

    @Override
    public int getDisplayHeight() {
        return tamed ? HEIGHT + 10 : HEIGHT;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(type.getDescription(), 1, 0, 0xFF404040, false);
        addEntity(widgets, parent, 0, 10, 40, HEIGHT);

        widgets.addSlot(food, 46, 12).appendTooltip(
            Component.translatable("emi_compendium.recipe.breeding.food"));
        if (!spawnEgg.isEmpty()) {
            widgets.addSlot(spawnEgg, 46, 32).appendTooltip(
                Component.translatable("emi_compendium.recipe.breeding.spawn_egg"));
        }
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 68, 22);
        if (!offspring.isEmpty()) {
            widgets.addSlot(offspring, 94, 18).large(true).recipeContext(this).appendTooltip(
                Component.translatable("emi_compendium.recipe.breeding.offspring"));
        }
        if (baby != null) {
            addEntity(widgets, baby, 122, 18, 156, HEIGHT);
        }
        if (tamed) {
            widgets.addText(Component.translatable("emi_compendium.recipe.breeding.tamed"),
                1, HEIGHT + 1, 0xFF404040, false);
        }
    }

    private void addEntity(WidgetHolder widgets, LivingEntity entity, int x0, int y0, int x1, int y1) {
        int width = x1 - x0;
        int height = y1 - y0;
        widgets.addDrawable(x0, y0, width, height, (graphics, mouseX, mouseY, delta) ->
            InventoryScreen.extractEntityInInventoryFollowsMouse(
                graphics, 0, 0, width, height, entitySize, 0.0625F, mouseX, mouseY, entity));
    }
}
