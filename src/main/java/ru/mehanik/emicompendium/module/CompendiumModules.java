package ru.mehanik.emicompendium.module;

import dev.emi.emi.api.EmiRegistry;
import ru.mehanik.emicompendium.EmiCompendium;
import ru.mehanik.emicompendium.api.CompendiumModule;
import ru.mehanik.emicompendium.module.vanilla.BeaconConduitModule;
import ru.mehanik.emicompendium.module.vanilla.BreedingModule;
import ru.mehanik.emicompendium.module.vanilla.VillagerProfessionModule;
import ru.mehanik.emicompendium.module.vanilla.MobEffectModule;
import dev.emi.emi.api.EmiInitRegistry;

import java.util.List;

public final class CompendiumModules {
    private static final List<CompendiumModule> MODULES =
        List.of(
            new BeaconConduitModule(),
            new VillagerProfessionModule(),
            new MobEffectModule(),
            new BreedingModule()
        );

    private CompendiumModules() {
    }

    /** Own modules that duplicate a bundled jar module are skipped (breeding → Just Enough Breeding). */
    private static boolean isEnabled(CompendiumModule module) {
        return !("breeding".equals(module.id())
            && net.neoforged.fml.ModList.get().isLoaded("justenoughbreeding"));
    }

    public static void initializeAll(EmiInitRegistry registry) {
        for (CompendiumModule module : MODULES) {
            if (!isEnabled(module)) continue;
            module.initialize(registry);
        }
    }

    public static void registerAll(EmiRegistry registry) {
        EmiCompendium.LOGGER.info(
            "Registering {} EMI Compendium modules",
            MODULES.size()
        );

        for (CompendiumModule module : MODULES) {
            if (!isEnabled(module)) {
                EmiCompendium.LOGGER.info("Skipping EMI module {} (covered by a bundled mod)", module.id());
                continue;
            }
            EmiCompendium.LOGGER.info(
                "Registering EMI module: {}",
                module.id()
            );

            module.register(registry);
        }
    }
}
