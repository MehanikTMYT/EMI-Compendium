package ru.mehtm.emicompendium.module.smithing;

import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.level.LevelEvent;
import ru.mehtm.emicompendium.EmiCompendium;

/**
 * Client half of the smithing trim preview: the config screen and the
 * recreation of preview armor stands when a client level is (re)loaded.
 *
 * Ported from SmithingTemplateViewer by Buuz135 (MIT).
 * This class is never loaded on a dedicated server.
 */
@EventBusSubscriber(modid = EmiCompendium.MOD_ID, value = Dist.CLIENT)
public final class SmithingViewerClient {
    private SmithingViewerClient() {
    }

    /** Called from the mod constructor on the client only. */
    public static void register(ModContainer container) {
        container.registerExtensionPoint(
            IConfigScreenFactory.class,
            ConfigurationScreen::new
        );
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ClientLevel level) {
            SmithingTrimPreview.INSTANCES.forEach(
                preview -> preview.recreateArmorStand(level)
            );
        }
    }
}
