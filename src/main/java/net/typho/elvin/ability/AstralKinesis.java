package net.typho.elvin.ability;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.typho.elvin.Elvin;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class AstralKinesis implements Ability {
    public static final Color LIGHT = new Color(255, 208, 114), DARK = new Color(229, 143, 57);

    @Override
    public String name() {
        return "astralkinesis";
    }

    public static class Implosion extends Explosion {
        public final World world;
        public final double x, y, z;
        public final float power;

        public Implosion(World world, @Nullable Entity entity, double x, double y, double z, float power, List<BlockPos> affectedBlocks) {
            super(world, entity, x, y, z, power, affectedBlocks);
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
        }

        public Implosion(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, DestructionType destructionType, List<BlockPos> affectedBlocks) {
            super(world, entity, x, y, z, power, createFire, destructionType, affectedBlocks);
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
        }

        public Implosion(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, DestructionType destructionType) {
            super(world, entity, x, y, z, power, createFire, destructionType);
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
        }

        public Implosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, DestructionType destructionType) {
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
                        Elvin.ASTRAL_BOOM_SOUND,
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
                            ).normalize());
                            fall.velocityModified = true;
                        }
                    }
                }
            }
        }
    }
}
