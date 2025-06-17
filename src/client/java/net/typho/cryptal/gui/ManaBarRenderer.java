package net.typho.cryptal.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.typho.cryptal.Cryptal;

import java.awt.*;

public class ManaBarRenderer implements HudRenderCallback {
    public static final Color FULL = new Color(38, 146, 255), EMPTY = new Color(19, 38, 76);
    private static final int X = 10, WIDTH = 10, HEIGHT = 100;

    public void onHudRender(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        float mana = Cryptal.getMana(client.player);

        int y = client.getWindow().getScaledHeight() / 2 - HEIGHT / 2;

        int filled = (int) ((mana / 10) * HEIGHT);

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
                Text.literal((int) (mana / 10 * 100) + "%"),
                X + WIDTH / 2,
                y + HEIGHT + 16,
                Color.WHITE.getRGB()
        );
    }
}
