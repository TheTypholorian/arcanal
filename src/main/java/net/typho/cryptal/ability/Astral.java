package net.typho.cryptal.ability;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.typho.cryptal.Cryptal;
import org.jetbrains.annotations.Nullable;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;

import java.awt.*;
import java.util.List;

public class Astral implements Ability {
    public static final Color LIGHT = new Color(255, 208, 114), DARK = new Color(229, 143, 57);

    public final Skill[] skills = new Skill[1];

    public Astral() {
        skills[0] = new ShockwaveSkill(this);
    }

    public Astral(NbtCompound nbt) {
        skills[0] = new ShockwaveSkill(this, nbt);
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
        public ShockwaveSkill(Ability parent) {
            super(parent);
        }

        public ShockwaveSkill(Ability parent, NbtCompound nbt) {
            super(parent, nbt);
        }

        @Override
        public float cost() {
            return 3;
        }

        @Override
        public float cooldown() {
            return 4;
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

            if (hit instanceof BlockHitResult) {
                target = hit.getPos();

                Cryptal.LOGGER.info("Hit at {}", target);

                if (!world.isClient) {
                    Explosion e = new Astral.Shockwave(world, player, world.getDamageSources().magic(), new EntityExplosionBehavior(player), target.x, target.y, target.z, 6, false, Explosion.DestructionType.DESTROY);
                    e.collectBlocksAndDamageEntities();
                    e.affectWorld(false);
                } else {
                    len = (float) target.distanceTo(origin);
                }
            }

            if (world.isClient) {
                ColorParticleData color = ColorParticleData.create(Astral.LIGHT, Astral.DARK).build();

                WorldParticleBuilder builder = WorldParticleBuilder.create(LodestoneParticleRegistry.SPARKLE_PARTICLE)
                        .setScaleData(GenericParticleData.create(0.5f, 0.1f, 0f).build())
                        .setRandomMotion(0.01)
                        .setColorData(color);

                Vec3d spawn = origin.add(look);
                Vec3d inc = look.multiply(0.125f);

                for (float i = 1; i < len; i += 0.125f) {
                    builder.setLifetime(40 + (int) (Math.random() * 20))
                            .spawn(world, spawn.x, spawn.y, spawn.z);
                    spawn = spawn.add(inc);
                }

                builder = WorldParticleBuilder.create(LodestoneParticleRegistry.STAR_PARTICLE)
                        .setScaleData(GenericParticleData.create(10f, 0f).build())
                        .setRandomMotion(0.1)
                        .setColorData(color)
                        .setLifetime(40 + (int) (Math.random() * 20));

                for (int i = 0; i < 5; i++) {
                    builder.spawn(world, target.x, target.y, target.z);
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

        @Override
        public void affectWorld(boolean particles) {
            if (world.isClient) {
                world.playSound(
                        x,
                        y,
                        z,
                        Cryptal.ASTRAL_BOOM_SOUND,
                        SoundCategory.BLOCKS,
                        4f,
                        (1f + (world.random.nextFloat() - world.random.nextFloat()) * 0.2f) * 0.7f,
                        false
                );
            } else {
                boolean bl = this.shouldDestroy();
                if (particles) {
                    if (!(power < 2) && bl) {
                        this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0, 0.0, 0.0);
                    } else {
                        this.world.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
                    }
                }

                if (bl) {
                    Entity cause = getCausingEntity();

                    for (BlockPos pos : getAffectedBlocks()) {
                        BlockState state = world.getBlockState(pos);

                        if (state.isAir()) {
                            continue;
                        }

                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);

                        FallingBlockEntity fall = FallingBlockEntity.spawnFromBlock(world, pos, state);

                        fall.setGlowing(true);

                        if (cause != null) {
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
