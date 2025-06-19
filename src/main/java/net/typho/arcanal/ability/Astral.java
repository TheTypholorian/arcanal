package net.typho.arcanal.ability;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.typho.arcanal.Arcanal;
import net.typho.arcanal.ArcanalClient;
import org.jetbrains.annotations.Nullable;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;

import java.awt.*;
import java.util.List;

public class Astral implements Ability {
    public static final Astral INSTANCE = new Astral();
    public static final Color LIGHT = new Color(255, 208, 114), DARK = new Color(229, 143, 57);
    public static final ColorParticleData COLOR_DATA = ColorParticleData.create(Astral.LIGHT, Astral.DARK).build();

    public static WorldParticleBuilder sparkles() {
        return WorldParticleBuilder.create(LodestoneParticleRegistry.TWINKLE_PARTICLE)
                .setRandomMotion(0.01)
                .setColorData(COLOR_DATA);
    }

    public static WorldParticleBuilder stars() {
        return WorldParticleBuilder.create(LodestoneParticleRegistry.STAR_PARTICLE)
                .setRandomMotion(0.1)
                .setSpinData(SpinParticleData.create(0.1f, 0f).build())
                .setColorData(COLOR_DATA);
    }

    public final Skill[] skills = new Skill[2];

    public Astral() {
        skills[0] = new ShockwaveSkill();
        skills[1] = new Skill.Flamethrower() {
            @Override
            public WorldParticleBuilder particles() {
                return WorldParticleBuilder.create(LodestoneParticleRegistry.TWINKLE_PARTICLE)
                        .setRandomMotion(0.25)
                        .setRandomOffset(0.5)
                        .setScaleData(GenericParticleData.create(0.5f, 0.2f, 0f).build())
                        .setSpinData(SpinParticleData.create(0.1f, 0f).build())
                        .setColorData(COLOR_DATA);
            }

            @Override
            public int numParticles() {
                return 20;
            }

            @Override
            public DamageSource damageSource(World world, PlayerEntity player) {
                return new DamageSource(Arcanal.damageSource(world, Arcanal.SOLAR_FLARE_DAMAGE_KEY), player);
            }

            @Override
            public int fireSeconds() {
                return 20;
            }

            @Override
            public int fireDamage() {
                return 6;
            }
        };
    }

    @Override
    public void clientTick(ClientWorld world, ClientPlayerEntity player) {
        while (ArcanalClient.MAJOR_KEYBINDING.wasPressed()) {
            skills[0].castToServer();
        }

        while (ArcanalClient.MINOR_KEYBINDING.wasPressed()) {
            skills[1].castToServer();
        }
    }

    @Override
    public String name() {
        return "astral";
    }

    @Override
    public Skill[] skills() {
        return skills;
    }

    public static class ShockwaveSkill extends Skill {
        @Override
        public float cost() {
            return 3;
        }

        @Override
        public String name() {
            return "astral_shockwave";
        }

        @Override
        public boolean cast(World world, PlayerEntity player) {
            if (!super.cast(world, player)) {
                return false;
            }

            Vec3d origin = player.getPos().add(0, player.getStandingEyeHeight(), 0);
            Vec3d look = player.getRotationVector();
            float len = 64;

            Vec3d target = origin.add(look.multiply(len));
            RaycastContext ctx = new RaycastContext(origin, target, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player);
            HitResult hit = world.raycast(ctx);

            target = hit.getPos();

            if (!world.isClient) {
                Explosion e = new Astral.Shockwave(world, player, world.getDamageSources().magic(), new EntityExplosionBehavior(player), target.x, target.y, target.z, 8, false, Explosion.DestructionType.DESTROY);
                e.collectBlocksAndDamageEntities();
                e.affectWorld(false);
            } else {
                len = (float) target.distanceTo(origin);
                Astral.Shockwave.boom(world, target.x, target.y, target.z);
            }

            if (world.isClient) {
                Vec3d spawn = origin.add(look);
                Vec3d inc = look.multiply(0.125f);

                WorldParticleBuilder sparkles = sparkles()
                        .setScaleData(GenericParticleData.create(0.5f, 0.1f, 0f).build());

                for (float i = 1; i < len; i += 0.125f) {
                    sparkles.setLifetime(40 + (int) (Math.random() * 20))
                            .spawn(world, spawn.x, spawn.y, spawn.z);
                    spawn = spawn.add(inc);
                }

                WorldParticleBuilder stars = stars()
                        .setScaleData(GenericParticleData.create(5f, 2f, 0f).build());

                for (int i = 0; i < 5; i++) {
                    stars.setLifetime(40 + (int) (Math.random() * 20))
                            .spawn(world, target.x, target.y, target.z);
                }
            }

            return true;
        }
    }

    public static class Shockwave extends Explosion {
        public final World world;
        public final double x, y, z;
        public final float power;

        public Shockwave(World world, @Nullable Entity entity, double x, double y, double z, float power, List<BlockPos> affectedBlocks) {
            super(world, entity, x, y, z, power, affectedBlocks);
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
        }

        public Shockwave(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, DestructionType destructionType, List<BlockPos> affectedBlocks) {
            super(world, entity, x, y, z, power, createFire, destructionType, affectedBlocks);
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
        }

        public Shockwave(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, DestructionType destructionType) {
            super(world, entity, x, y, z, power, createFire, destructionType);
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
        }

        public Shockwave(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, DestructionType destructionType) {
            super(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType);
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
        }

        public static void boom(World world, double x, double y, double z) {
            world.playSound(
                    x,
                    y,
                    z,
                    Arcanal.ASTRAL_BOOM_SOUND,
                    SoundCategory.BLOCKS,
                    4f,
                    (1f + (world.random.nextFloat() - world.random.nextFloat()) * 0.2f) * 0.7f,
                    false
            );
        }

        @Override
        public void affectWorld(boolean particles) {
            if (world.isClient) {
                boom(world, x, y, z);
            } else {
                boolean bl = this.shouldDestroy();
                if (particles) {
                    if (!(power < 2) && bl) {
                        this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1, 0, 0);
                    } else {
                        this.world.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1, 0, 0);
                    }
                }

                if (bl) {
                    Entity cause = getCausingEntity();

                    if (cause != null) {
                        for (BlockPos pos : getAffectedBlocks()) {
                            BlockState state = world.getBlockState(pos);

                            if (state.isAir()) {
                                continue;
                            }

                            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);

                            FallingBlockEntity fall = FallingBlockEntity.spawnFromBlock(world, pos, state);

                            fall.setVelocity(new Vec3d(
                                    cause.getX() - fall.getX() + world.getRandom().nextFloat() * 4 - 2,
                                    cause.getY() - fall.getY() + 3 + world.getRandom().nextFloat() * 4 - 2,
                                    cause.getZ() - fall.getZ() + world.getRandom().nextFloat() * 4 - 2
                            ).normalize().multiply(world.getRandom().nextFloat()));
                            fall.velocityModified = true;
                        }
                    }
                }
            }
        }
    }
}
