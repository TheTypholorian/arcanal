package net.typho.elvin;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import net.typho.elvin.ability.AstralKinesis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;

public class Elvin implements ModInitializer {
	public static final String MOD_ID = "elvin";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final EntityAttribute MANA_ATTRIBUTE = Registry.register(Registries.ATTRIBUTE, new Identifier(MOD_ID, "mana"), new ClampedEntityAttribute(MOD_ID + ".mana", 0, 0, 10).setTracked(true));

	public static final Item WAND = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "wand"), new Item(new FabricItemSettings()) {
		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
			ItemStack held = player.getStackInHand(hand);
			Vec3d origin = player.getPos().add(0, player.getStandingEyeHeight(), 0);
			Vec3d look = player.getRotationVector();
			float len = 512;

			Vec3d target = origin.add(look.multiply(len));
			RaycastContext ctx = new RaycastContext(origin, target, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player);
			HitResult hit = world.raycast(ctx);

			if (hit instanceof BlockHitResult block) {
				Vec3d pos = hit.getPos();
				Explosion e = new AstralKinesis.Implosion(world, player, world.getDamageSources().magic(), new EntityExplosionBehavior(player), pos.x, pos.y, pos.z, 6, false, Explosion.DestructionType.DESTROY);
				e.collectBlocksAndDamageEntities();
				e.affectWorld(false);

				len = (float) block.getPos().distanceTo(origin);
			}

			if (!world.isClient) {
				return TypedActionResult.pass(held);
			}

			ColorParticleData color = ColorParticleData.create(AstralKinesis.LIGHT, AstralKinesis.DARK).build();

			WorldParticleBuilder builder = WorldParticleBuilder.create(LodestoneParticleRegistry.SPARKLE_PARTICLE)
					.setScaleData(GenericParticleData.create(1f, 0.2f, 0f).build())
					.setRandomMotion(0.01)
					.setColorData(color);

			for (float i = 0; i < len; i += 0.25f) {
				Vec3d spawn = origin.add(look.multiply(i));
				builder.setLifetime(40 + (int) (Math.random() * 20))
						.spawn(world, spawn.x, spawn.y, spawn.z);
			}

			Vec3d spawn = origin.add(look.multiply(len));

			builder = WorldParticleBuilder.create(LodestoneParticleRegistry.STAR_PARTICLE)
					.setScaleData(GenericParticleData.create(10f, 0f).build())
					.setRandomMotion(0.1)
					.setColorData(color)
					.setLifetime(40 + (int) (Math.random() * 20));

			for (int i = 0; i < 5; i++) {
				builder.spawn(world, spawn.x, spawn.y, spawn.z);
			}

			return TypedActionResult.success(held);
		}
	});

	public static final SoundEvent ASTRAL_BOOM_SOUND = sound("astral_boom");

	private static SoundEvent sound(String name) {
		Identifier id = new Identifier(MOD_ID, name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	@Override
	public void onInitialize() {
		FabricDefaultAttributeRegistry.register(EntityType.PLAYER, PlayerEntity.createPlayerAttributes().add(MANA_ATTRIBUTE, 0));
	}
}