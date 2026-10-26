package ru.mehtm.emicompendium.module.professions;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import ru.mehtm.emicompendium.EmiCompendium;
import ru.mehtm.emicompendium.api.CompendiumModule;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Villager professions and their job-site blocks (vanilla and modded).
 *
 * <p>Idea and lookup approach from EMI Professions by Mrbysco (MIT, https://github.com/Mrbysco/EMIProfessions),
 * rewritten for Minecraft 26.1 (holders, render-state GUI).
 */
public final class VillagerProfessionModule implements CompendiumModule {

    public static final EmiRecipeCategory PROFESSION =
        new EmiRecipeCategory(
            EmiCompendium.id("villager_profession"),
            EmiStack.of(Items.VILLAGER_SPAWN_EGG)
        );

    @Override
    public String id() {
        return "villager_professions";
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(PROFESSION);
        registry.addWorkstation(PROFESSION, EmiStack.of(Items.EMERALD));
        registry.addWorkstation(PROFESSION, EmiStack.of(Items.VILLAGER_SPAWN_EGG));

        List<Holder.Reference<PoiType>> poiTypes =
            BuiltInRegistries.POINT_OF_INTEREST_TYPE.listElements().toList();

        BuiltInRegistries.VILLAGER_PROFESSION.listElements().forEach(profession -> {
            if (profession.is(VillagerProfession.NONE)) {
                return;
            }
            Set<Item> workstations = new LinkedHashSet<>();
            for (Holder.Reference<PoiType> poi : poiTypes) {
                if (!profession.value().acquirableJobSite().test(poi)) {
                    continue;
                }
                for (BlockState state : poi.value().matchingStates()) {
                    Item item = state.getBlock().asItem();
                    if (item != Items.AIR) {
                        workstations.add(item);
                    }
                }
            }
            if (workstations.isEmpty()) {
                return;
            }
            List<EmiStack> stacks = new ArrayList<>();
            workstations.forEach(item -> stacks.add(EmiStack.of(item)));
            registry.addRecipe(new VillagerProfessionRecipe(PROFESSION, profession, stacks));
        });
    }
}
