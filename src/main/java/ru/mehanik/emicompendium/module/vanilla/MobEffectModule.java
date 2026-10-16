package ru.mehanik.emicompendium.module.vanilla;

import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiRegistryAdapter;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import ru.mehanik.emicompendium.EmiCompendium;
import ru.mehanik.emicompendium.api.CompendiumModule;
import ru.mehanik.emicompendium.recipe.MobEffectInfoRecipe;
import ru.mehanik.emicompendium.stack.MobEffectEmiStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Status effects: one page per effect with its description and sources; effects appear in the EMI index.
 *
 * <p>Ported from EMIffect (MIT, Flamarine / Prismwork, https://git.sr.ht/~flamarine/emiffect) — descriptions in
 * assets/emi_compendium/lang come from it as well. 26.1: food effects live in the CONSUMABLE component.
 */
public final class MobEffectModule implements CompendiumModule {

    public static final EmiRecipeCategory EFFECT =
        new EmiRecipeCategory(
            EmiCompendium.id("mob_effect"),
            EmiStack.of(Items.POTION)
        );

    @Override
    public String id() {
        return "mob_effects";
    }

    @Override
    public void initialize(EmiInitRegistry registry) {
        registry.addIngredientSerializer(MobEffectEmiStack.class, new MobEffectEmiStack.Serializer());
        registry.addRegistryAdapter(EmiRegistryAdapter.simple(
            MobEffect.class,
            BuiltInRegistries.MOB_EFFECT,
            (effect, arg, amount) -> new MobEffectEmiStack(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect))
        ));
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(EFFECT);
        registry.addWorkstation(EFFECT, EmiStack.of(Items.POTION));
        registry.addWorkstation(EFFECT, EmiStack.of(Items.BEACON));

        Map<MobEffect, List<EmiIngredient>> sources = new LinkedHashMap<>();
        BuiltInRegistries.MOB_EFFECT.forEach(effect -> sources.put(effect, new ArrayList<>()));

        BuiltInRegistries.POTION.listElements().forEach((Holder.Reference<Potion> potion) -> {
            for (MobEffectInstance instance : potion.value().getEffects()) {
                List<EmiIngredient> list = sources.get(instance.getEffect().value());
                if (list == null) continue;
                for (Item item : List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW)) {
                    list.add(EmiStack.of(PotionContents.createItemStack(item, potion)));
                }
            }
        });

        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof FlowerBlock flower) {
                for (SuspiciousStewEffects.Entry entry : flower.getSuspiciousEffects().effects()) {
                    List<EmiIngredient> list = sources.get(entry.effect().value());
                    if (list != null) {
                        ItemStack stew = new ItemStack(Items.SUSPICIOUS_STEW);
                        stew.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, new SuspiciousStewEffects(List.of(entry)));
                        list.add(EmiStack.of(stew));
                    }
                }
            }
        }

        for (Item item : BuiltInRegistries.ITEM) {
            Consumable consumable = item.components().get(DataComponents.CONSUMABLE);
            if (consumable == null) continue;
            consumable.onConsumeEffects().forEach(consumeEffect -> {
                if (consumeEffect instanceof ApplyStatusEffectsConsumeEffect apply) {
                    for (MobEffectInstance instance : apply.effects()) {
                        List<EmiIngredient> list = sources.get(instance.getEffect().value());
                        if (list != null) list.add(EmiStack.of(item));
                    }
                }
            });
        }

        for (List<Holder<MobEffect>> tier : BeaconBlockEntity.BEACON_EFFECTS) {
            for (Holder<MobEffect> effect : tier) {
                List<EmiIngredient> list = sources.get(effect.value());
                if (list != null && !list.contains(EmiStack.of(Blocks.BEACON))) list.add(EmiStack.of(Blocks.BEACON));
            }
        }

        sources.forEach((effect, list) -> {
            MobEffectEmiStack stack = new MobEffectEmiStack(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
            registry.addEmiStack(stack);
            registry.addRecipe(new MobEffectInfoRecipe(EFFECT, stack, List.copyOf(list)));
        });
    }
}
