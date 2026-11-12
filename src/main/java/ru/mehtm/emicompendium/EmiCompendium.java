package ru.mehtm.emicompendium;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mehtm.emicompendium.module.smithing.SmithingViewerClient;
import ru.mehtm.emicompendium.module.smithing.SmithingViewerConfig;

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

        container.registerConfig(
            net.neoforged.fml.config.ModConfig.Type.COMMON,
            SmithingViewerConfig.SPEC
        );

        // Client-only classes (config screen, preview armor stands) must never
        // be touched on a dedicated server.
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            SmithingViewerClient.register(container);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(
            MOD_ID,
            path
        );
    }
}
