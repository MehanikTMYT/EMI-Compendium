package ru.mehtm.emicompendium.module.smithing;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Configuration of the smithing trim preview.
 *
 * Ported from SmithingTemplateViewer by Buuz135 (MIT),
 * https://github.com/Buuz135/SmithingTemplateViewer
 */
public final class SmithingViewerConfig {
    public static final SmithingViewerConfig CONFIG;
    public static final ModConfigSpec SPEC;

    public final ModConfigSpec.BooleanValue displayMaterials;

    private SmithingViewerConfig(ModConfigSpec.Builder builder) {
        displayMaterials = builder
            .comment(
                "Whether trim materials should be shown as EMI inputs"
            )
            .define("display_materials", true);
    }

    static {
        Pair<SmithingViewerConfig, ModConfigSpec> pair =
            new ModConfigSpec.Builder().configure(
                SmithingViewerConfig::new
            );

        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }
}
