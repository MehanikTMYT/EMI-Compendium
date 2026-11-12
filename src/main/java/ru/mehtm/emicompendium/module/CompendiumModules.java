package ru.mehtm.emicompendium.module;

import dev.emi.emi.api.EmiRegistry;
import ru.mehtm.emicompendium.EmiCompendium;
import ru.mehtm.emicompendium.api.CompendiumModule;
import ru.mehtm.emicompendium.module.professions.VillagerProfessionModule;
import ru.mehtm.emicompendium.module.smithing.SmithingViewerModule;
import ru.mehtm.emicompendium.module.vanilla.BeaconConduitModule;
import ru.mehtm.emicompendium.module.vanilla.BreedingModule;
import ru.mehtm.emicompendium.module.vanilla.MobEffectModule;
import dev.emi.emi.api.EmiInitRegistry;

import java.util.List;

public final class CompendiumModules {
    private static final List<CompendiumModule> MODULES =
        List.of(
            new BeaconConduitModule(),
            new VillagerProfessionModule(),
            new MobEffectModule(),
            new BreedingModule(),
            new SmithingViewerModule()
        );

    private CompendiumModules() {
    }

    /** Own modules that duplicate a bundled jar module are skipped (breeding → Just Enough Breeding). */
    private static boolean isEnabled(CompendiumModule module) {
        if ("breeding".equals(module.id())) {
            return !net.neoforged.fml.ModList.get().isLoaded("justenoughbreeding");
        }
        if ("smithing".equals(module.id())) {
            // The official Smithing Template Viewer may ship its own EMI plugin;
            // never register a second trim viewer category next to it.
            return !SmithingViewerModule.providesOwnEmiPlugin();
        }
        return true;
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
                EmiCompendium.LOGGER.info("Skipping EMI module {} (covered by another mod)", module.id());
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
