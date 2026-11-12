package ru.mehtm.emicompendium.module.smithing;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SmithingTemplateItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One smithing trim recipe rendered as an interactive armor preview:
 * template / base / material slots, armor stand, per-slot armour and
 * trim material selectors.
 *
 * Ported from SmithingTemplateViewer by Buuz135 (MIT),
 * https://github.com/Buuz135/SmithingTemplateViewer
 * (EMI plugin, adapted to EMI 1.1.24 and Minecraft 26.1.2).
 */
public final class SmithingViewerRecipe implements EmiRecipe {
    private final Identifier id;
    private final SmithingTrimPreview preview;

    public SmithingViewerRecipe(Identifier id, SmithingTrimPreview preview) {
        this.id = Identifier.fromNamespaceAndPath(
            id.getNamespace(),
            "/" + id.getPath() + "_viewer"
        );
        this.preview = preview;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return SmithingViewerModule.CATEGORY;
    }

    @Override
    public @Nullable Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        EmiIngredient addition;
        if (SmithingViewerConfig.CONFIG.displayMaterials.get()) {
            addition = EmiIngredient.of(preview.additionIngredient());
        } else {
            ItemStack any = Items.BARRIER.getDefaultInstance();
            any.set(
                DataComponents.ITEM_NAME,
                Component.translatable("emi_compendium.recipe.smithing.any_item")
            );
            addition = EmiStack.of(any);
        }

        return List.of(
            EmiIngredient.of(preview.templateIngredient()),
            EmiIngredient.of(preview.baseIngredient()),
            addition
        );
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return 120;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        List<EmiIngredient> inputs = getInputs();
        widgets.addSlot(inputs.get(0), 0, 0);
        widgets.addSlot(inputs.get(1), 0, 18);
        widgets.addSlot(inputs.get(2), 0, 36);

        int standX = 75;
        int standY = 75;

        widgets.addDrawable(0, 0, 0, 0, (graphics, mouseX, mouseY, delta) -> {
            ItemStack template = preview.getTemplate();
            if (template.getItem() instanceof SmithingTemplateItem) {
                graphics.text(
                    Minecraft.getInstance().font,
                    Component
                        .translatable("item.minecraft.smithing_template.upgrade")
                        .withStyle(ChatFormatting.DARK_GRAY),
                    20,
                    5,
                    0xFFFFFF,
                    false
                );
            }

            Identifier inventory =
                Identifier.withDefaultNamespace(
                    "textures/gui/container/inventory.png"
                );
            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                inventory,
                standX - 25,
                standY - 57,
                25,
                7,
                51,
                72,
                256,
                256
            );

            // 26.1.2: renderEntityInInventory was removed; clip the stand to the
            // panel exactly like the official Smithing Template Viewer build.
            ScreenRectangle panel = new ScreenRectangle(
                standX - 25,
                standY - 57,
                50,
                72
            ).transformAxisAligned(graphics.pose());

            InventoryScreen.renderEntityInInventoryFollowsAngle(
                graphics,
                panel.left(),
                panel.top(),
                panel.right(),
                panel.bottom(),
                25,
                0.0F,
                0.0F,
                0.0F,
                preview.getArmorStand()
            );

            graphics.item(
                preview.getAdditionItems().get(preview.getColorIndex()),
                standX - 24 + 17,
                standY + 20
            );
        });

        for (int i = 0; i < 4; i++) {
            int slot = i;
            widgets.addButton(
                standX - 25 - 16,
                standY - 57 + 4 + 18 * i,
                12,
                12,
                0,
                0,
                () -> preview.getArmorIndex()[slot] > 0,
                (mouseX, mouseY, button) -> {
                    if (preview.getArmorIndex()[slot] > 0) {
                        preview.getArmorIndex()[slot]--;
                        preview.updateArmorStand(Minecraft.getInstance().level);
                    }
                }
            );
            widgets.addButton(
                standX + 25 + 4,
                standY - 57 + 4 + 18 * i,
                12,
                12,
                12,
                0,
                () ->
                    preview.getArmorIndex()[slot] <
                    preview.getArmors().get(slot).size(),
                (mouseX, mouseY, button) -> {
                    if (
                        preview.getArmorIndex()[slot] <
                        preview.getArmors().get(slot).size()
                    ) {
                        preview.getArmorIndex()[slot]++;
                        preview.updateArmorStand(Minecraft.getInstance().level);
                    }
                }
            );
        }

        int maxColor = preview.getAdditionItems().size() - 1;
        widgets.addButton(
            standX - 22,
            standY + 22,
            12,
            12,
            0,
            0,
            () -> preview.getColorIndex() > 0,
            (mouseX, mouseY, button) -> {
                if (preview.getColorIndex() > 0) {
                    preview.setColorIndex(preview.getColorIndex() - 1);
                    preview.updateArmorStand(Minecraft.getInstance().level);
                }
            }
        );
        widgets.addButton(
            standX + 12,
            standY + 22,
            12,
            12,
            12,
            0,
            () -> preview.getColorIndex() < maxColor,
            (mouseX, mouseY, button) -> {
                if (preview.getColorIndex() < maxColor) {
                    preview.setColorIndex(preview.getColorIndex() + 1);
                    preview.updateArmorStand(Minecraft.getInstance().level);
                }
            }
        );
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
}
