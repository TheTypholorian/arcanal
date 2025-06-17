package net.typho.cryptal;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.typho.cryptal.ability.Ability;
import net.typho.cryptal.ability.Astral;
import net.typho.cryptal.ability.ManaRegen;
import net.typho.cryptal.ability.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Cryptal implements ModInitializer {
	public static final String MOD_ID = "cryptal";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final EntityAttribute MANA_ATTRIBUTE = Registry.register(Registries.ATTRIBUTE, new Identifier(MOD_ID, "mana"), new ClampedEntityAttribute(MOD_ID + ".mana", 0, 0, 10).setTracked(true));
	public static TrackedData<NbtCompound> ABILITY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

	public static Ability getAbility(PlayerEntity player) {
		return Ability.fromNbt(player.getDataTracker().get(ABILITY));
	}

	public static void setAbility(PlayerEntity player, Ability ability) {
		player.getDataTracker().set(ABILITY, ability.toNbt(new NbtCompound()));
	}

	public static final SoundEvent ASTRAL_BOOM_SOUND = sound("astral_boom");

	private static SoundEvent sound(String name) {
		Identifier id = new Identifier(MOD_ID, name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	@Override
	public void onInitialize() {
		Ability.FROM_NBT_MAP.put("astral", Astral::new);
		Ability.FROM_NBT_MAP.put("none", nbt -> new Ability.None());
		FabricDefaultAttributeRegistry.register(EntityType.PLAYER, PlayerEntity.createPlayerAttributes().add(MANA_ATTRIBUTE, 0));
		ServerPlayNetworking.registerGlobalReceiver(
				Skill.CAST_PACKET_ID,
				(server, player, handler, buf, responseSender) -> {
					LOGGER.info("Received cast packet");

					EntityAttributeInstance mana = player.getAttributeInstance(Cryptal.MANA_ATTRIBUTE);

					Ability ability = getAbility(player);

					if (ability != null) {
						String target = buf.readString();

						for (Skill skill : ability.skills()) {
							if (Objects.equals(skill.name(), target)) {
								if (skill.cast(player.getWorld(), player)) {
									if (mana != null && mana.getAttribute() instanceof ClampedEntityAttribute clamp) {
										mana.setBaseValue(clamp.clamp(mana.getBaseValue() - skill.cost()));
									}
								}

								break;
							}
						}
					}
				}
		);
		ServerTickEvents.START_SERVER_TICK.register(new ManaRegen());
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				literal("ability")
						.executes(ctx -> {
							ctx.getSource().sendFeedback(
									() -> Text.literal("Your ability is " + getAbility(Objects.requireNonNull(ctx.getSource().getPlayer())).name()),
									false
							);
							return 1;
						})
						.then(
								argument("type", StringArgumentType.word())
										.suggests((ctx, builder) -> {
											for (String ability : Ability.FROM_NBT_MAP.keySet()) {
												builder.suggest(ability);
											}

											return builder.buildFuture();
										})
										.executes(ctx -> {
											String type = StringArgumentType.getString(ctx, "type");

											try {
												NbtCompound nbt = new NbtCompound();
												nbt.putString("name", type);
												setAbility(Objects.requireNonNull(ctx.getSource().getPlayer()), Ability.fromNbt(nbt));
												return 1;
											} catch (NullPointerException e) {
												ctx.getSource().sendFeedback(
														() -> Text.literal("No ability " + type).setStyle(Style.EMPTY.withColor(Formatting.RED)),
														false
												);
												return 0;
											}
										})
						)
		));
	}
}