package net.typho.cryptal.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.typho.cryptal.Cryptal;

import java.awt.*;

public class ManaBarRenderer implements HudRenderCallback {
    public static final Color FULL = new Color(38, 146, 255), EMPTY = new Color(19, 38, 76);
    private static final int X = 10, WIDTH = 10, HEIGHT = 100;

    public void onHudRender(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        EntityAttributeInstance inst = client.player.getAttributeInstance(Cryptal.MANA_ATTRIBUTE);
        assert inst != null;
        float mana = (float) inst.getValue();
        int maxMana = (int) ((ClampedEntityAttribute) inst.getAttribute()).getMaxValue();

        int y = client.getWindow().getScaledHeight() / 2 - HEIGHT / 2;

        int filled = (int) ((mana / maxMana) * HEIGHT);

        context.fill(X, y, X + WIDTH, y + HEIGHT, EMPTY.getRGB());
        context.fill(X, y + HEIGHT - filled, X + WIDTH, y + HEIGHT, FULL.getRGB());

        context.drawCenteredTextWithShadow(
                client.textRenderer,
                Text.literal("Mana"),
                X + WIDTH / 2,
                y + HEIGHT + 6,
                Color.WHITE.getRGB()
        );
        context.drawCenteredTextWithShadow(
                client.textRenderer,
                Text.literal((int) (mana / maxMana * 100) + "%"),
                X + WIDTH / 2,
                y + HEIGHT + 16,
                Color.WHITE.getRGB()
        );
    }
}
