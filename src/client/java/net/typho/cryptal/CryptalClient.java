package net.typho.cryptal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.typho.cryptal.ability.Ability;
import net.typho.cryptal.ability.Astral;
import net.typho.cryptal.gui.ManaBarRenderer;
import org.lwjgl.glfw.GLFW;

public class CryptalClient implements ClientModInitializer {
	public static final String ASTRAL_KEY_CATEGORY = "key.category" + Cryptal.MOD_ID + ".astral";
	public static final KeyBinding ASTRAL_BOOM_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Cryptal.MOD_ID + ".astral.boom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, ASTRAL_KEY_CATEGORY));

	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register(new ManaBarRenderer());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (ASTRAL_BOOM_KEYBINDING.wasPressed()) {
				assert client.player != null;
				Ability ability = Cryptal.getAbility(client.player);
				Cryptal.LOGGER.info(ability.name());

				if (ability instanceof Astral astral) {
					astral.skills[0].cast(client.player.getWorld(), client.player);
				}
			}
		});
	}
}