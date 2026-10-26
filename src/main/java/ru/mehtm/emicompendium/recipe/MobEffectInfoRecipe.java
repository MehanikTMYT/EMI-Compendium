package ru.mehtm.emicompendium.recipe;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;
import ru.mehtm.emicompendium.EmiCompendium;
import ru.mehtm.emicompendium.stack.MobEffectEmiStack;

import java.util.List;

/**
 * Status effect page: description and every known source (potions, arrows, food, suspicious stew, beacon).
 *
 * <p>Layout and paging from EMIffect's MobEffectInfo (MIT, Flamarine / Prismwork), rewritten for 26.1. The description
 * is split lazily (on the render thread — font glyphs can't be loaded on EMI's reload worker).
 */
public final class MobEffectInfoRecipe implements EmiRecipe {
    private static final int WIDTH = 144;
    private static final int COLUMNS = 6;

    private final EmiRecipeCategory category;
    private final MobEffectEmiStack effect;
    private final List<EmiIngredient> sources;
    private final Identifier id;
    private @Nullable List<FormattedCharSequence> description;

    public MobEffectInfoRecipe(EmiRecipeCategory category, MobEffectEmiStack effect, List<EmiIngredient> sources) {
        this.category = category;
        this.effect = effect;
        this.sources = sources;
        Identifier key = effect.getId();
        this.id = EmiCompendium.id("/effect/" + key.getNamespace() + "/" + key.getPath());
    }

    private List<FormattedCharSequence> description() {
        if (description == null) {
            Identifier key = effect.getId();
            String first = "effect." + key.getNamespace() + "." + key.getPath() + ".description";
            String second = "effect." + key.getNamespace() + "." + key.getPath() + ".desc";
            Component text = I18n.exists(first) ? Component.translatable(first)
                : I18n.exists(second) ? Component.translatable(second)
                : Component.translatable("emi_compendium.effect.no_description").withStyle(ChatFormatting.GRAY);
            description = Minecraft.getInstance().font.split(text, WIDTH - 31);
        }
        return description;
    }

    private int upperOffset() {
        return 14 + Math.max(description().size() * Minecraft.getInstance().font.lineHeight, 30) + 2;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return sources;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(effect);
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        int rows = sources.isEmpty() ? 0 : (sources.size() - 1) / COLUMNS + 1;
        return Math.min(upperOffset() + rows * 18, 160);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var font = Minecraft.getInstance().font;
        int titleColor = switch (effect.effect().value().getCategory()) {
            case BENEFICIAL -> 0xFF55FF55;
            case NEUTRAL -> 0xFFFFAA00;
            case HARMFUL -> 0xFFFF5555;
        };
        FormattedCharSequence title = effect.getName().getVisualOrderText();
        widgets.addText(title, 31 + (WIDTH - 31 - font.width(title)) / 2, 2, titleColor, true);
        List<FormattedCharSequence> desc = description();
        for (int i = 0; i < desc.size(); i++) {
            widgets.addText(desc.get(i), 31, 14 + font.lineHeight * i, 0xFFFFFFFF, true);
        }
        int descHeight = Math.max(desc.size() * font.lineHeight, 30) + 12;
        widgets.add(new SlotWidget(effect, 3, (descHeight - 26) / 2).large(true).recipeContext(this));

        if (sources.isEmpty()) {
            return;
        }
        int upper = upperOffset();
        int visibleRows = Math.max(1, (widgets.getHeight() - upper) / 18);
        int totalRows = (sources.size() - 1) / COLUMNS + 1;
        Pages pages = new Pages(sources, visibleRows * COLUMNS);
        if (visibleRows < totalRows) {
            widgets.addButton(2, upper, 12, 12, 0, 0, () -> true, (mx, my, button) -> pages.scroll(-1));
            widgets.addButton(widgets.getWidth() - 14, upper, 12, 12, 12, 0, () -> true, (mx, my, button) -> pages.scroll(1));
        }
        for (int i = 0; i < pages.size; i++) {
            widgets.add(new PageSlot(pages, i, i % COLUMNS * 18 + 18, i / COLUMNS * 18 + upper));
        }
    }

    private static final class Pages {
        final List<EmiIngredient> stacks;
        final int size;
        int page;

        Pages(List<EmiIngredient> stacks, int size) {
            this.stacks = stacks;
            this.size = size;
        }

        void scroll(int delta) {
            int total = (stacks.size() - 1) / size + 1;
            page = Math.floorMod(page + delta, total);
        }

        EmiIngredient get(int offset) {
            int i = page * size + offset;
            return i < stacks.size() ? stacks.get(i) : EmiStack.EMPTY;
        }
    }

    private static final class PageSlot extends SlotWidget {
        private final Pages pages;
        private final int offset;

        PageSlot(Pages pages, int offset, int x, int y) {
            super(EmiStack.EMPTY, x, y);
            this.pages = pages;
            this.offset = offset;
        }

        @Override
        public EmiIngredient getStack() {
            return pages.get(offset);
        }

        @Override
        public dev.emi.emi.api.recipe.EmiRecipe getRecipe() {
            return EmiApi.getRecipeContext(getStack());
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor draw, int mouseX, int mouseY, float delta) {
            if (!getStack().isEmpty()) {
                super.extractRenderState(draw, mouseX, mouseY, delta);
            }
        }
    }
}
