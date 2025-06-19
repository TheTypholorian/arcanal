package net.typho.arcanal.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.typho.arcanal.Arcanal;

import java.awt.*;

public class ManaBarRenderer implements HudRenderCallback {
    public static final Color FULL = new Color(38, 146, 255), EMPTY = new Color(19, 38, 76);
    private static final int Y = 15, WIDTH = 100, HEIGHT = 10;

    public void onHudRender(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        float mana = Arcanal.getMana(client.player);

        int x = client.getWindow().getScaledWidth() / 2 - WIDTH / 2;
        int y = client.getWindow().getScaledHeight() / 2 + Y;

        int filled = (int) ((mana / 10) * HEIGHT);

        context.fill(x, y, x + WIDTH, y + HEIGHT, EMPTY.getRGB());
        context.fill(x, y + HEIGHT, x + filled, y + HEIGHT, FULL.getRGB());

        context.drawCenteredTextWithShadow(
                client.textRenderer,
                Text.literal("Mana (" + (int) (mana / 10 * 100) + "%)"),
                x + WIDTH / 2,
                y + HEIGHT / 2,
                Color.WHITE.getRGB()
        );
    }
}
