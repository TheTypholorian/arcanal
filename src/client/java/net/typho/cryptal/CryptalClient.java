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
import net.typho.cryptal.ability.Astral;
import net.typho.cryptal.ability.Skill;
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

				if (ability instanceof Astral astral) {
					Skill skill = astral.skills[0];

					if (skill.canCast(client.player)) {
						PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

						buf.writeString(skill.name());

						ClientPlayNetworking.send(Skill.CAST_TO_SERVER_PACKET_ID, buf);
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