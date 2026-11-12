package ru.mehtm.emicompendium.module.smithing;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Preview state of one smithing trim recipe: the armor stand and the
 * per-slot armour / trim material selections of the EMI viewer.
 *
 * Ported from SmithingTemplateViewer by Buuz135 (MIT),
 * https://github.com/Buuz135/SmithingTemplateViewer
 *
 * Client-only: referenced from the EMI plugin, never on the server.
 */
public final class SmithingTrimPreview {
    public static final List<SmithingTrimPreview> INSTANCES = new ArrayList<>();

    private final SmithingTrimRecipe recipe;
    private ArmorStand armorStand;
    private final int[] armorIndex;
    private int colorIndex;
    private final List<List<ItemStack>> armors;

    public SmithingTrimPreview(SmithingTrimRecipe recipe) {
        this.recipe = recipe;
        this.armorIndex = new int[]{1, 1, 1, 1};
        this.colorIndex = 0;
        this.armors = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            this.armors.add(new ArrayList<>());
        }
        // Base items are taken from the recipe's EQUIPPABLE component, so armour
        // of other mods is listed as soon as it declares an equipment slot.
        for (ItemStack item : getItems(baseIngredient())) {
            Equippable equippable = item.get(DataComponents.EQUIPPABLE);
            if (equippable != null
                && equippable.slot().getIndex() >= 0
                && equippable.slot().getIndex() < 4) {
                this.armors
                    .get(3 - equippable.slot().getIndex())
                    .add(item.copy());
            }
        }
        recreateArmorStand(Minecraft.getInstance().level);
        INSTANCES.add(this);
    }

    public void recreateArmorStand(Level level) {
        this.armorStand = new ArmorStand(level, 0.0, 0.0, 0.0);
        this.armorStand.setNoBasePlate(true);
        this.armorStand.setShowArms(true);
        this.armorStand.yBodyRot = 210.0F;
        this.armorStand.setXRot(25.0F);
        this.armorStand.yHeadRot = this.armorStand.getYRot();
        this.armorStand.yHeadRotO = this.armorStand.getYRot();
        this.updateArmorStand(level);
    }

    public SmithingTrimRecipe getRecipe() {
        return recipe;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public int[] getArmorIndex() {
        return armorIndex;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public List<List<ItemStack>> getArmors() {
        return armors;
    }

    public void updateArmorStand(Level level) {
        EquipmentSlot[] slots = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
        };
        for (int i = 0; i < 4; i++) {
            int index = this.armorIndex[i];
            if (index != 0) {
                ItemStack stack = this.getArmors().get(i).get(index - 1);
                stack = recipe.assemble(
                    new SmithingRecipeInput(
                        getTemplate(),
                        stack.copy(),
                        getAdditionItems().get(getColorIndex())
                    )
                );
                this.armorStand.setItemSlot(slots[i], stack.copy());
            } else {
                this.armorStand.setItemSlot(slots[i], ItemStack.EMPTY);
            }
        }
    }

    public ItemStack getTemplate() {
        return getItems(templateIngredient()).getFirst();
    }

    public List<ItemStack> getAdditionItems() {
        return getItems(additionIngredient());
    }

    /** 26.1.2: the recipe fields are private, these are the public accessors. */
    public Ingredient templateIngredient() {
        return recipe.templateIngredient().orElseGet(Ingredient::of);
    }

    public Ingredient baseIngredient() {
        return recipe.baseIngredient();
    }

    public Ingredient additionIngredient() {
        return recipe.additionIngredient().orElseGet(Ingredient::of);
    }

    private static List<ItemStack> getItems(Ingredient ingredient) {
        return ingredient
            .items()
            .map(holder -> holder.value().getDefaultInstance())
            .toList();
    }
}
