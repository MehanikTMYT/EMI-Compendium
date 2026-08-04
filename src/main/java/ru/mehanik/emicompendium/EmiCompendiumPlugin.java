package ru.mehanik.emicompendium;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import ru.mehanik.emicompendium.module.CompendiumModules;

@EmiEntrypoint
public final class EmiCompendiumPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        EmiCompendium.LOGGER.info(
            "EMI discovered EMI Compendium plugin"
        );

        CompendiumModules.registerAll(registry);
    }
}
