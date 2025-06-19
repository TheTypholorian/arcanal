package net.typho.arcanal;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.typho.arcanal.ability.Ability;
import net.typho.arcanal.ability.Astral;
import net.typho.arcanal.ability.ManaComponent;
import net.typho.arcanal.ability.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Arcanal implements ModInitializer, EntityComponentInitializer {
	public static final String MOD_ID = "arcanal";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final RegistryKey<DamageType> SOLAR_FLARE_DAMAGE_KEY = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Arcanal.MOD_ID, "solar_flare"));

	public static RegistryEntry.Reference<DamageType> damageSource(World world, RegistryKey<DamageType> key) {
		return world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key);
	}

	public static final ComponentKey<Ability.Component> ABILITY_COMPONENT = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(MOD_ID, "ability"), Ability.Component.class);
	public static final ComponentKey<ManaComponent> MANA_COMPONENT = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(MOD_ID, "mana"), ManaComponent.class);

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(ABILITY_COMPONENT, Ability.Component::new, RespawnCopyStrategy.ALWAYS_COPY);
		registry.registerForPlayers(MANA_COMPONENT, ManaComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
	}

	public static Ability getAbility(PlayerEntity player) {
		return player.getComponent(ABILITY_COMPONENT).getAbility();
	}

	public static void setAbility(PlayerEntity player, Ability ability) {
		player.getComponent(ABILITY_COMPONENT).setAbility(ability);
	}

	public static float getMana(PlayerEntity player) {
		return player.getComponent(MANA_COMPONENT).getMana();
	}

	public static void setMana(PlayerEntity player, float mana) {
		player.getComponent(MANA_COMPONENT).setMana(mana);
	}

	public static final SoundEvent ASTRAL_BOOM_SOUND = sound("supernova");

	private static SoundEvent sound(String name) {
		Identifier id = new Identifier(MOD_ID, name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	public static HitResult raycast(World world, Entity entity, double range) {
		Vec3d posEye = entity.getEyePos();
		Vec3d dirVec = entity.getRotationVector();
		Vec3d endVec = posEye.add(dirVec.multiply(range));
		RaycastContext context = new RaycastContext(
				posEye,
				endVec,
				RaycastContext.ShapeType.OUTLINE,
				RaycastContext.FluidHandling.NONE,
				entity
		);
		BlockHitResult blockHit = world.raycast(context);
		Box boxArea = entity.getBoundingBox()
				.stretch(dirVec.multiply(range))
				.expand(1);
		EntityHitResult entityHit = ProjectileUtil.getEntityCollision(
				world,
				entity,
				posEye,
				endVec,
				boxArea,
				e -> true
		);

		if (entityHit != null) {
			if (entityHit.getPos().squaredDistanceTo(posEye) < blockHit.getPos().squaredDistanceTo(posEye)) {
				return entityHit;
			}
		}

		return blockHit;
	}

	@Override
	public void onInitialize() {
		Ability.put(Ability.None.INSTANCE, Astral.INSTANCE);
		ServerPlayNetworking.registerGlobalReceiver(
				Skill.CAST_TO_SERVER_PACKET_ID,
				(server, player, handler, buf, responseSender) -> {
					LOGGER.info("Received cast packet");

					Ability ability = getAbility(player);

					if (ability != null) {
						Skill skill = ability.getSkill(buf.readString());

						server.execute(() -> {
							if (skill.cast(player.getWorld(), player)) {
								setMana(player, getMana(player) - skill.cost());
							}
						});
					}
				}
		);
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
											for (String ability : Ability.ABILITY_MAP.keySet()) {
												builder.suggest(ability);
											}

											return builder.buildFuture();
										})
										.executes(ctx -> {
											String type = StringArgumentType.getString(ctx, "type");

											try {
												setAbility(Objects.requireNonNull(ctx.getSource().getPlayer()), Ability.ABILITY_MAP.get(type));
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