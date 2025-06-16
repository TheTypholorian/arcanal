package net.typho.elvin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.typho.elvin.gui.ManaBarRenderer;

public class ElvinClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register(ManaBarRenderer::onHudRender);
	}
}