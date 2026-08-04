package ru.mehanik.emicompendium.module;

import dev.emi.emi.api.EmiRegistry;
import ru.mehanik.emicompendium.EmiCompendium;
import ru.mehanik.emicompendium.api.CompendiumModule;

import java.util.List;

public final class CompendiumModules {
    private static final List<CompendiumModule> MODULES =
        List.of();

    private CompendiumModules() {
    }

    public static void registerAll(EmiRegistry registry) {
        EmiCompendium.LOGGER.info(
            "Registering {} EMI Compendium modules",
            MODULES.size()
        );

        for (CompendiumModule module : MODULES) {
            EmiCompendium.LOGGER.info(
                "Registering EMI module: {}",
                module.id()
            );

            module.register(registry);
        }
    }
}
