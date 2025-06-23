package net.typho.arcanal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.typho.arcanal.ability.Ability;
import net.typho.arcanal.ability.Skill;
import net.typho.arcanal.gui.ManaBarRenderer;
import org.lwjgl.glfw.GLFW;

public class ArcanalClient implements ClientModInitializer {
	public static final String KEY_CATEGORY = "key.category." + Arcanal.MOD_ID;
	public static final KeyBinding KEYBINDING_1 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Arcanal.MOD_ID + ".1", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_4, KEY_CATEGORY));
	public static final KeyBinding KEYBINDING_2 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Arcanal.MOD_ID + ".2", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, KEY_CATEGORY));
	public static final KeyBinding KEYBINDING_3 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Arcanal.MOD_ID + ".3", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_3, KEY_CATEGORY));

	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register(new ManaBarRenderer());
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (client.player != null) {
				Ability ability = Arcanal.getAbility(client.player);

				if (ability != null) {
					ability.clientTick(client.world, client.player);
				}
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(
				Skill.CAST_TO_CLIENT_PACKET_ID,
				(client, handler, buf, responseSender) -> {
					String skillName = buf.readString();
					int sourceID = buf.readInt();

					client.execute(() -> {
                        assert client.world != null;
                        Entity source = client.world.getEntityById(sourceID);

						if (source instanceof PlayerEntity player) {
							if (player.getWorld() == client.world) {
								Arcanal.getAbility(player).getSkill(player, skillName).cast(player.getWorld(), player);
							}
						}
					});
				}
		);
	}
}