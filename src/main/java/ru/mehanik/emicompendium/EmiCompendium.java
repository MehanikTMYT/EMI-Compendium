package ru.mehanik.emicompendium;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(EmiCompendium.MOD_ID)
public final class EmiCompendium {
    public static final String MOD_ID = "emi_compendium";

    public static final Logger LOGGER =
        LoggerFactory.getLogger("EMI Compendium");

    public EmiCompendium(ModContainer container) {
        LOGGER.info(
            "Loading EMI Compendium {}",
            container.getModInfo().getVersion()
        );
    }
}
