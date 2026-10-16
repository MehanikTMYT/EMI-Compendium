package ru.mehanik.emicompendium.module.vanilla;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import ru.mehanik.emicompendium.EmiCompendium;
import ru.mehanik.emicompendium.api.CompendiumModule;
import ru.mehanik.emicompendium.recipe.BreedingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Animal breeding: food items, tame requirement and offspring (vanilla and modded).
 *
 * <p>Idea and layout from Just Enough Breeding by Christofmeg (MIT, https://github.com/Christofmeg/JustEnoughBreeding).
 * Instead of the donor's per-mod recipe JSON, breeding data is probed from the entity registry:
 * {@link Animal#isFood} for the food and {@link Animal#canMate} on a pair of probe entities for breedability.
 */
public final class BreedingModule implements CompendiumModule {

    public static final EmiRecipeCategory BREEDING =
        new EmiRecipeCategory(
            EmiCompendium.id("breeding"),
            EmiStack.of(Items.WHEAT)
        );

    /** Animals whose offspring comes from a laid block item rather than directly. */
    private static final Map<EntityType<?>, Item> LAID_OFFSPRING = Map.of(
        EntityType.TURTLE, Items.TURTLE_EGG,
        EntityType.SNIFFER, Items.SNIFFER_EGG,
        EntityType.FROG, Items.FROGSPAWN
    );

    @Override
    public String id() {
        return "breeding";
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(BREEDING);

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        List<ItemStack> allItems = BuiltInRegistries.ITEM.stream()
            .filter(item -> item != Items.AIR)
            .map(ItemStack::new)
            .toList();

        BuiltInRegistries.ENTITY_TYPE.listElements().forEach(type -> {
            if (type.value().getCategory() == MobCategory.MISC) {
                return;
            }
            try {
                BreedingRecipe recipe = probe(type, level, allItems);
                if (recipe != null) {
                    registry.addRecipe(recipe);
                }
            } catch (RuntimeException | LinkageError e) {
                EmiCompendium.LOGGER.debug("Skipping breeding info for {}", type.key().identifier(), e);
            }
        });
    }

    private static @Nullable BreedingRecipe probe(
        Holder.Reference<EntityType<?>> type,
        Level level,
        List<ItemStack> allItems
    ) {
        if (!(type.value().create(level, EntitySpawnReason.LOAD) instanceof Animal parent)
            || !(type.value().create(level, EntitySpawnReason.LOAD) instanceof Animal partner)
            || !parent.canFallInLove()) {
            return null;
        }
        parent.setInLoveTime(600);
        partner.setInLoveTime(600);
        boolean tamed = false;
        if (!parent.canMate(partner)) {
            if (!tame(parent) || !tame(partner) || !parent.canMate(partner)) {
                return null;
            }
            tamed = true;
        }
        parent.resetLove();

        List<Item> foods = foods(parent, allItems);
        // Some tamables mate untamed but treat food as taming items until tamed (nautilus): breeding then needs taming.
        if (!tamed && tame(partner)) {
            List<Item> tamedFoods = foods(partner, allItems);
            if (!tamedFoods.equals(foods)) {
                foods = tamedFoods;
                tamed = true;
                tame(parent);
            }
        }
        if (foods.isEmpty()) {
            return null;
        }

        EmiStack spawnEgg = SpawnEggItem.byId(type.value())
            .map(egg -> EmiStack.of(egg.value()))
            .orElse(EmiStack.EMPTY);
        Item laid = LAID_OFFSPRING.get(type.value());
        EmiStack offspring = laid != null ? EmiStack.of(laid) : spawnEgg;

        AgeableMob baby = null;
        if (laid == null && type.value().create(level, EntitySpawnReason.LOAD) instanceof AgeableMob child) {
            child.setBaby(true);
            baby = child;
        }
        return new BreedingRecipe(
            BREEDING,
            type,
            parent,
            baby,
            EmiIngredient.of(Ingredient.of(foods.stream())),
            spawnEgg,
            offspring,
            tamed
        );
    }

    private static List<Item> foods(Animal animal, List<ItemStack> allItems) {
        List<Item> foods = new ArrayList<>();
        for (ItemStack stack : allItems) {
            if (animal.isFood(stack)) {
                foods.add(stack.getItem());
            }
        }
        return foods;
    }

    private static boolean tame(Entity entity) {
        if (entity instanceof TamableAnimal tamable) {
            tamable.setTame(true, false);
            return true;
        }
        if (entity instanceof AbstractHorse horse) {
            horse.setTamed(true);
            return true;
        }
        return false;
    }
}
