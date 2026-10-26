package ru.mehtm.emicompendium.module.professions;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import ru.mehtm.emicompendium.EmiCompendium;

import java.util.List;

/**
 * A villager profession: the villager (rendered with that profession) and the blocks it takes as a job site.
 * Not part of the recipe tree.
 *
 * <p>Holding Shift shows the profession entity id (the same fallback EMI Professions provides).
 */
public final class VillagerProfessionRecipe implements EmiRecipe {
    private static final int WIDTH = 124;
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
        return WIDTH;
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
        widgets.addDrawable(0, 0, WIDTH, 10, (graphics, mouseX, mouseY, delta) -> extractName(graphics));
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

    /** Name text, switching to the profession id while Shift is held (like EMI Professions). Scaled when too long. */
    private void extractName(net.minecraft.client.gui.GuiGraphicsExtractor graphics) {
        var font = Minecraft.getInstance().font;
        var window = Minecraft.getInstance().getWindow();
        boolean shift = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
            || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        Identifier key = profession.key().identifier();
        String text = shift
            ? key.getNamespace() + ":" + key.getPath()
            : profession.value().name().getString();
        int width = font.width(text);
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(1, 1);
        if (width > WIDTH - 12) {
            pose.scale(0.75F);
        }
        graphics.text(font, text, 0, 0, 0xFF404040, false);
        pose.popMatrix();
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
