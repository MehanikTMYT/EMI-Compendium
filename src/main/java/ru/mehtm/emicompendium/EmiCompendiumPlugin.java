package ru.mehtm.emicompendium;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import ru.mehtm.emicompendium.module.CompendiumModules;

@EmiEntrypoint
public final class EmiCompendiumPlugin implements EmiPlugin {
    @Override
    public void initialize(EmiInitRegistry registry) {
        CompendiumModules.initializeAll(registry);
    }

    @Override
    public void register(EmiRegistry registry) {
        EmiCompendium.LOGGER.info(
            "EMI discovered EMI Compendium plugin"
        );

        CompendiumModules.registerAll(registry);
    }
}
