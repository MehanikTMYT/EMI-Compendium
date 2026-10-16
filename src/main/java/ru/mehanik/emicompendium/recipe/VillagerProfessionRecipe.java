package ru.mehanik.emicompendium.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import org.jspecify.annotations.Nullable;
import ru.mehanik.emicompendium.EmiCompendium;

import java.util.List;

/**
 * A villager profession: the villager (rendered with that profession) and the blocks it takes as a job site.
 * Not part of the recipe tree.
 */
public final class VillagerProfessionRecipe implements EmiRecipe {
    private static @Nullable Villager cachedVillager;

    private final EmiRecipeCategory category;
    private final Holder.Reference<VillagerProfession> profession;
    private final EmiIngredient workstations;
    private final List<EmiIngredient> inputs;
    private final Identifier id;

    public VillagerProfessionRecipe(
        EmiRecipeCategory category,
        Holder.Reference<VillagerProfession> profession,
        List<EmiStack> workstations
    ) {
        this.category = category;
        this.profession = profession;
        this.workstations = EmiIngredient.of(workstations);
        this.inputs = List.copyOf(workstations);
        Identifier key = profession.key().identifier();
        this.id = EmiCompendium.id("/villager_profession/" + key.getNamespace() + "/" + key.getPath());
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
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return 124;
    }

    @Override
    public int getDisplayHeight() {
        return 62;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(profession.value().name(), 1, 0, 0xFF404040, false);
        widgets.addDrawable(22, 10, 50, 52, (graphics, mouseX, mouseY, delta) -> {
            Villager villager = villager();
            if (villager != null) {
                InventoryScreen.extractEntityInInventoryFollowsMouse(
                    graphics, 0, 0, 50, 52, 24, 0.0625F, mouseX, mouseY, villager);
            }
        });
        widgets.addSlot(workstations, 92, 22).appendTooltip(
            net.minecraft.network.chat.Component.translatable("emi_compendium.recipe.villager_profession.workstation"));
    }

    private @Nullable Villager villager() {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        if (cachedVillager == null || cachedVillager.level() != level) {
            cachedVillager = new Villager(EntityType.VILLAGER, level);
        }
        cachedVillager.setVillagerData(cachedVillager.getVillagerData().withProfession(profession));
        return cachedVillager;
    }
}
