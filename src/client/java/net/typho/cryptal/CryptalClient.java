package net.typho.cryptal;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.typho.cryptal.ability.Ability;
import net.typho.cryptal.ability.Skill;
import net.typho.cryptal.gui.ManaBarRenderer;
import org.lwjgl.glfw.GLFW;

public class CryptalClient implements ClientModInitializer {
	public static final String KEY_CATEGORY = "key.category." + Cryptal.MOD_ID;
	public static final KeyBinding MAJOR_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Cryptal.MOD_ID + ".major", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_4, KEY_CATEGORY));
	public static final KeyBinding MINOR_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Cryptal.MOD_ID + ".minor", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, KEY_CATEGORY));

	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register(new ManaBarRenderer());

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (client.player != null) {
				Ability ability = Cryptal.getAbility(client.player);

				if (ability != null) {
					while (MAJOR_KEYBINDING.wasPressed()) {
						Skill skill = ability.majorSkill();

						if (skill != null) {
							PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
							buf.writeString(skill.name());
							ClientPlayNetworking.send(Skill.CAST_TO_SERVER_PACKET_ID, buf);
						}
					}

					while (MINOR_KEYBINDING.wasPressed()) {
						Skill skill = ability.minorSkill();

						if (skill != null) {
							PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
							buf.writeString(skill.name());
							ClientPlayNetworking.send(Skill.CAST_TO_SERVER_PACKET_ID, buf);
						}
					}

					if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_SPACE)) {
						Skill skill = ability.passiveSkill();

						if (skill != null) {
							PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
							buf.writeString(skill.name());
							ClientPlayNetworking.send(Skill.CAST_TO_SERVER_PACKET_ID, buf);
						}
					}
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
								Cryptal.getAbility(player).getSkill(skillName).cast(player.getWorld(), player);
							}
						}
					});
				}
		);
	}
}