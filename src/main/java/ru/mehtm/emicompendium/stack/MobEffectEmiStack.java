package ru.mehtm.emicompendium.stack;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A status effect as an EMI stack (shown in the index, searchable, favouritable).
 *
 * <p>Based on MobEffectEmiStack from EMIffect by Flamarine / Prismwork (MIT, https://git.sr.ht/~flamarine/emiffect),
 * rewritten for Minecraft 26.1 (holders, GUI sprite atlas via {@link Gui#getMobEffectSprite}).
 */
public final class MobEffectEmiStack extends EmiStack {
    private final Holder<MobEffect> effect;

    public MobEffectEmiStack(Holder<MobEffect> effect) {
        this.effect = effect;
    }

    public Holder<MobEffect> effect() {
        return effect;
    }

    @Override
    public EmiStack copy() {
        return new MobEffectEmiStack(effect).setAmount(getAmount()).setChance(getChance())
            .setRemainder(getRemainder()).comparison(comparison);
    }

    @Override
    public void render(GuiGraphicsExtractor draw, int x, int y, float delta, int flags) {
        draw.blitSprite(RenderPipelines.GUI_TEXTURED, Gui.getMobEffectSprite(effect), x - 1, y - 1, 18, 18);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public DataComponentPatch getComponentChanges() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    public Object getKey() {
        return effect.value();
    }

    @Override
    public Identifier getId() {
        return effect.unwrapKey().orElseThrow().identifier();
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of(getName());
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        List<ClientTooltipComponent> tooltip = new ArrayList<>();
        tooltip.add(ClientTooltipComponent.create(getName().getVisualOrderText()));
        MobEffect value = effect.value();
        String category = switch (value.getCategory()) {
            case BENEFICIAL -> "beneficial";
            case NEUTRAL -> "neutral";
            case HARMFUL -> "harmful";
        };
        ChatFormatting color = switch (value.getCategory()) {
            case BENEFICIAL -> ChatFormatting.GREEN;
            case NEUTRAL -> ChatFormatting.GOLD;
            case HARMFUL -> ChatFormatting.RED;
        };
        tooltip.add(ClientTooltipComponent.create(
            Component.translatable("emi_compendium.effect." + category).withStyle(color).getVisualOrderText()));
        Component swatch = Component.translatable("emi_compendium.effect.color",
                "#" + String.format(Locale.ROOT, "%06X", value.getColor() & 0xFFFFFF))
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(" █").withStyle(s -> s.withColor(TextColor.fromRgb(value.getColor()))));
        tooltip.add(ClientTooltipComponent.create(swatch.getVisualOrderText()));
        tooltip.add(ClientTooltipComponent.create(Component.literal(
                net.neoforged.fml.ModList.get().getModContainerById(getId().getNamespace())
                    .map(c -> c.getModInfo().getDisplayName()).orElse(getId().getNamespace()))
            .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC).getVisualOrderText()));
        return tooltip;
    }

    @Override
    public Component getName() {
        return effect.value().getDisplayName();
    }

    public static final class Serializer implements EmiStackSerializer<MobEffectEmiStack> {
        @Override
        public EmiStack create(Identifier id, DataComponentPatch componentChanges, long amount) {
            return BuiltInRegistries.MOB_EFFECT.get(id)
                .<EmiStack>map(effect -> new MobEffectEmiStack(effect).setAmount(amount))
                .orElse(EmiStack.EMPTY);
        }

        @Override
        public String getType() {
            return "emi_compendium_mob_effect";
        }
    }
}
