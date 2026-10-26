package ru.mehtm.emicompendium.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A non-crafting informational relation.
 *
 * It connects an item or group of items with a target block while
 * remaining excluded from EMI's recipe tree.
 */
public final class StaticInfoRecipe implements EmiRecipe {
    private final EmiRecipeCategory category;
    private final Identifier id;
    private final EmiIngredient input;
    private final EmiStack output;
    private final Component title;
    private final Component inputTooltip;
    private final Component firstLine;
    private final Component secondLine;

    public StaticInfoRecipe(
        EmiRecipeCategory category,
        Identifier id,
        EmiIngredient input,
        EmiStack output,
        Component title,
        Component inputTooltip,
        Component firstLine,
        Component secondLine
    ) {
        this.category = category;
        this.id = id;
        this.input = input;
        this.output = output;
        this.title = title;
        this.inputTooltip = inputTooltip;
        this.firstLine = firstLine;
        this.secondLine = secondLine;
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
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return 62;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(
            title,
            4,
            5,
            0xFFFFFF,
            true
        );

        widgets.addSlot(
            input,
            12,
            29
        ).appendTooltip(inputTooltip);

        widgets.addText(
            firstLine,
            36,
            28,
            0xB0B0B0,
            false
        );

        widgets.addText(
            secondLine,
            36,
            41,
            0xB0B0B0,
            false
        );

        widgets.addSlot(
            output,
            116,
            25
        )
            .large(true)
            .recipeContext(this);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
}
