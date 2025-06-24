package net.typho.arcanal.ability;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.typho.arcanal.Arcanal;
import net.typho.arcanal.ArcanalClient;
import org.jetbrains.annotations.Nullable;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;
import team.lodestar.lodestone.systems.screenshake.PositionedScreenshakeInstance;

import java.awt.*;
import java.util.List;

public class Astral implements Ability {
    public static final Color LIGHT = new Color(255, 208, 114), DARK = new Color(229, 143, 57);
    public static final ColorParticleData COLOR_DATA = ColorParticleData.create(Astral.LIGHT, Astral.DARK).build();
    public static final Astral INSTANCE = new Astral();

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

    public final Skill[] skills = new Skill[3];

    public Astral() {
        skills[0] = new SupernovaSkill();
        skills[1] = new Skill.Shotgun() {
            @Override
            public WorldParticleBuilder particles() {
                return WorldParticleBuilder.create(LodestoneParticleRegistry.TWINKLE_PARTICLE)
                        .setRandomMotion(0.15)
                        .setRandomOffset(0.25)
                        .setScaleData(GenericParticleData.create(0.5f, 0.2f, 0f).build())
                        .setSpinData(SpinParticleData.create(0.1f, 0f).build())
                        .setColorData(COLOR_DATA);
            }

            @Override
            public int numParticles() {
                return 5;
            }

            @Override
            public String name() {
                return "solar_flare";
            }

            @Override
            public Text desc() {
                return Skill.defDesc(cost(), keybind(), LIGHT, "Solar Flare", "Creates a power 3 explosion that creates fire but doesn't destroy blocks, good in combat.");
            }

            @Override
            public KeyBinding keybind() {
                return ArcanalClient.KEYBINDING_2;
            }

            @Override
            public boolean castServer(ServerWorld world, ServerPlayerEntity player, Vec3d origin, Vec3d dir, Vec3d target) {
                Explosion e = new Explosion(world, player, target.x, target.y, target.z, 3, true, Explosion.DestructionType.KEEP);
                e.collectBlocksAndDamageEntities();
                e.affectWorld(explosionParticles());

                return true;
            }
        };
        skills[2] = new GravitySkill();
    }

    @Override
    public String name() {
        return "astral";
    }

    @Override
    public Text desc() {
        return Ability.defDesc("Astral", LIGHT, skills, "Inflicts hit entities with the Glowing effect for 1.5 seconds. Attacking a glowing entity causes 2x base damage, buffing combos.");
    }

    @Override
    public Skill[] skills(PlayerEntity player) {
        return skills;
    }

    @Override
    public void onAttack(PlayerEntity attacker, Entity target) {
        if (target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 30, 0, false, false, true));
        }
    }

    @Override
    public Text getDeathMessage(LivingEntity killed, LivingEntity killer) {
        return killed.getDisplayName().copy().append(Text.translatable("death.arcanal.astral"));
    }

    @Override
    public float getDamage(float damage, PlayerEntity attacker, LivingEntity target) {
        if (target.isGlowing()) {
            return damage * 2;
        }

        return damage;
    }

    public static class SupernovaSkill implements Skill.Missile {
        @Override
        public String name() {
            return "supernova";
        }

        @Override
        public Text desc() {
            return Skill.defDesc(cost(), keybind(), LIGHT, "Supernova", "Emits a power 6 explosion (creating fire) that converts all exploded blocks to falling blocks, launching them away from the origin. Shooting it at flat surfaces doesn't do much, but shooting in the middle of a structure will obliterate it.");
        }

        @Override
        public KeyBinding keybind() {
            return ArcanalClient.KEYBINDING_1;
        }

        @Override
        public boolean castClient(ClientWorld world, ClientPlayerEntity player, Vec3d origin, Vec3d dir, Vec3d target) {
            float len = (float) target.distanceTo(origin);
            Supernova.playSound(world, target.x, target.y, target.z);

            Vec3d spawn = origin.add(dir);
            Vec3d inc = dir.multiply(0.125f);

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

            ScreenshakeHandler.addScreenshake(new PositionedScreenshakeInstance(40, target, 20, 50).setIntensity(0.5f, 0.1f, 0f));

            return true;
        }

        @Override
        public boolean castServer(ServerWorld world, ServerPlayerEntity player, Vec3d origin, Vec3d dir, Vec3d target) {
            Explosion e = new Supernova(world, player, target.x, target.y, target.z, 6, true, Explosion.DestructionType.DESTROY);
            e.collectBlocksAndDamageEntities();
            e.affectWorld(explosionParticles());

            return true;
        }
    }

    public static class Supernova extends Explosion {
        public final World world;
        public final double x, y, z;
        public final float power;

        public Supernova(World world, @Nullable Entity entity, double x, double y, double z, float power, List<BlockPos> affectedBlocks) {
            super(world, entity, x, y, z, power, affectedBlocks);
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
        }

        public Supernova(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, DestructionType destructionType, List<BlockPos> affectedBlocks) {
            super(world, entity, x, y, z, power, createFire, destructionType, affectedBlocks);
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
        }

        public Supernova(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, DestructionType destructionType) {
            super(world, entity, x, y, z, power, createFire, destructionType);
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
        }

        public Supernova(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, DestructionType destructionType) {
            super(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType);
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.power = power;
        }

        public static void playSound(World world, double x, double y, double z) {
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
                playSound(world, x, y, z);
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

                            fall.dropItem = false;
                            fall.setVelocity(new Vec3d(
                                    pos.getX()- x,
                                    pos.getY() - y,
                                    pos.getZ() - z
                            ).multiply(0.1 + world.random.nextFloat() * 0.1));
                            fall.velocityModified = true;
                        }
                    }
                }
            }
        }
    }

    public static class GravitySkill implements Skill {
        @Override
        public float cost() {
            return 10;
        }

        @Override
        public String name() {
            return "gravity";
        }

        @Override
        public Text desc() {
            return Skill.defDesc(cost(), keybind(), LIGHT, "Gravity", "Pulls all players and mobs within a 10 block square radius towards you, inflicting all with the glowing effect. For all players, 10% mana is taken from them and heals 1 heart for you.");
        }

        @Override
        public KeyBinding keybind() {
            return ArcanalClient.KEYBINDING_3;
        }

        @Override
        public boolean cast(World world, PlayerEntity player) {
            if (!Skill.super.cast(world, player)) {
                return false;
            }

            List<LivingEntity> pull = world.getEntitiesByClass(LivingEntity.class, new Box(player.getPos().subtract(10, 10, 10), player.getPos().add(10, 10, 10)), e -> e != player);

            if (pull.isEmpty()) {
                return false;
            }

            if (world.isClient) {
                WorldParticleBuilder builder = WorldParticleBuilder.create(LodestoneParticleRegistry.TWINKLE_PARTICLE)
                        .setRandomOffset(0.25)
                        .setScaleData(GenericParticleData.create(0.5f, 0.2f, 0f).build())
                        .setSpinData(SpinParticleData.create(0.1f, 0f).build())
                        .setColorData(COLOR_DATA)
                        .enableNoClip();

                for (LivingEntity target : pull) {
                    Vec3d pos = target.getEyePos();
                    builder.setMotion(player.getPos().subtract(pos).normalize());

                    for (int i = 0; i < 5; i++) {
                        builder.spawn(world, pos.x, pos.y, pos.z);
                    }
                }
            } else {
                for (LivingEntity target : pull) {
                    target.setVelocity(player.getPos().subtract(target.getEyePos()).normalize());
                    target.velocityModified = true;
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 30, 0, false, false, true));

                    if (target instanceof PlayerEntity steal) {
                        float targetMana = Arcanal.getMana(steal);
                        float take = Math.min(targetMana, 1);

                        Arcanal.setMana(steal, targetMana - take);
                        player.heal(take);
                    }
                }
            }

            return true;
        }
    }
}
