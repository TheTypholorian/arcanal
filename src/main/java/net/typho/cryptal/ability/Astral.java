package net.typho.cryptal.ability;

import io.github.fabricators_of_create.porting_lib.util.KeyBindingHelper;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.typho.cryptal.Cryptal;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class Astral implements Ability {
    public static final Color LIGHT = new Color(255, 208, 114), DARK = new Color(229, 143, 57);

    private PlayerEntity parent;
    public final Skill[] skills = new Skill[1];

    public Astral() {
        skills[0] = new BoomSkill(this);
    }

    public Astral(NbtCompound nbt) {
    }

    @Override
    public PlayerEntity parent() {
        return parent;
    }

    @Override
    public void parent(PlayerEntity player) {
        parent = player;
    }

    @Override
    public String name() {
        return "astral";
    }

    @Override
    public Skill[] skills() {
        return skills;
    }

    public static class BoomSkill extends Skill {
        public BoomSkill(Ability parent) {
            super(parent);
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
            return "astral_boom";
        }

        @Override
        public void cast(World world, PlayerEntity player) {
            super.cast(world, player);
        }
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
