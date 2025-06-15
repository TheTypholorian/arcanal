package net.typho.elvin;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;

import java.awt.*;

public class Elvin implements ModInitializer {
	public static final String MOD_ID = "elvin";
	public static final Color ASTRAL_BRIGHT = new Color(255, 208, 114), ASTRAL_DARK = new Color(229, 143, 57);

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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
				if (!world.isClient) {
					world.createExplosion(player, world.getDamageSources().magic(), new EntityExplosionBehavior(player), hit.getPos(), 10, true, World.ExplosionSourceType.MOB);
				}

				len = (float) block.getPos().distanceTo(origin);
			}

			if (!world.isClient) {
				return TypedActionResult.pass(held);
			}

			ColorParticleData color = ColorParticleData.create(ASTRAL_BRIGHT, ASTRAL_DARK).build();

			WorldParticleBuilder builder = WorldParticleBuilder.create(LodestoneParticleRegistry.SPARKLE_PARTICLE)
					.setScaleData(GenericParticleData.create(1f, 0.2f, 0f).build())
					.setColorData(color);

			for (float i = 0; i < len; i += 0.25f) {
				Vec3d spawn = origin.add(look.multiply(i));
				builder.setLifetime(40 + (int) (Math.random() * 20))
						.spawn(world, spawn.x, spawn.y, spawn.z);
			}

			Vec3d spawn = origin.add(look.multiply(len));

			builder = WorldParticleBuilder.create(LodestoneParticleRegistry.STAR_PARTICLE)
					.setScaleData(GenericParticleData.create(10f, 0f).build())
					.setColorData(color)
					.setLifetime(40 + (int) (Math.random() * 20));

			for (int i = 0; i < 5; i++) {
				builder.spawn(world, spawn.x, spawn.y, spawn.z);
			}

			return TypedActionResult.success(held);
		}
	});

	@Override
	public void onInitialize() {
	}
}