package net.typho.arcanal.ability;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.typho.arcanal.Arcanal;
import net.typho.arcanal.ArcanalClient;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class Abyssal implements Ability {
    public static final Abyssal INSTANCE = new Abyssal();
    public static final Color LIGHT = new Color(41, 223, 235), DARK = new Color(5, 98, 93);
    public static final List<Supplier<Boolean>> SPREAD_TASKS = new LinkedList<>();

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> SPREAD_TASKS.removeIf(Supplier::get));
    }

    public final Skill[] skills = new Skill[3];

    public Abyssal() {
        skills[0] = new SonicBoomSkill();
        skills[1] = new CatalystSkill();
        skills[2] = new ShriekSkill();
    }

    @Override
    public String name() {
        return "abyssal";
    }

    @Override
    public Text desc() {
        return Ability.defDesc("Abyssal", LIGHT, skills, "Sculk sensors will not react to sounds made by you, nor will they carry the signal if you step on them. You'd have to step on a shrieker to activate it. Wardens will not target you. Attacking inflicts darkness for 3 seconds.");
    }

    @Override
    public Skill[] skills(PlayerEntity player) {
        return skills;
    }

    @Override
    public boolean cancelsSculk(GameEvent event) {
        return !event.isIn(Arcanal.ABYSSAL_WHITELIST);
    }

    @Override
    public void clientTick(ClientWorld world, ClientPlayerEntity player) {
        while (ArcanalClient.MAJOR_KEYBINDING.wasPressed()) {
            skills[0].castToServer();
        }

        while (ArcanalClient.MINOR_KEYBINDING.wasPressed()) {
            skills[1].castToServer();
        }

        while (ArcanalClient.MOBILITY_KEYBINDING.wasPressed()) {
            skills[2].castToServer();
        }
    }

    public static class SonicBoomSkill implements Skill.Missile {
        @Override
        public String name() {
            return "sonic_boom";
        }

        @Override
        public Text desc() {
            return Skill.defDesc(cost(), LIGHT, "Sonic Boom", "Throws a Warden's Sonic Boom in the direction you're looking at, unleashing a 6 power no-fire, no-break explosion at the end.");
        }

        @Override
        public Explosion explosion(World world, PlayerEntity player, Vec3d pos) {
            return new Astral.Supernova(world, player, pos.x, pos.y, pos.z, 6, false, Explosion.DestructionType.KEEP);
        }

        @Override
        public boolean castServer(ServerWorld world, ServerPlayerEntity player, Vec3d origin, Vec3d dir, Vec3d target) {
            Vec3d spawn = origin.add(dir);
            float len = (float) target.distanceTo(spawn);

            for (int i = 0; i < len; i++) {
                world.spawnParticles(ParticleTypes.SONIC_BOOM, spawn.x, spawn.y, spawn.z, 1, 0, 0, 0, 0);
                spawn = spawn.add(dir);
            }

            world.playSound(null, target.x, target.y, target.z, SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 3, 1);

            return true;
        }
    }

    public static class CatalystSkill implements Skill {
        @Override
        public float cost() {
            return 4;
        }

        @Override
        public String name() {
            return "catalyst";
        }

        @Override
        public Text desc() {
            return Skill.defDesc(cost(), LIGHT, "Catalyst", "Takes up to 100 xp points from you and converts it to sculk at your cursor.");
        }

        @Override
        public boolean cast(World world, PlayerEntity player) {
            if (!Skill.super.cast(world, player)) {
                return false;
            }

            if (player.totalExperience == 0) {
                return false;
            }

            Vec3d origin = player.getEyePos();
            Vec3d look = player.getRotationVector();

            Vec3d target = origin.add(look.multiply(16));
            RaycastContext ctx = new RaycastContext(origin, target, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player);
            HitResult hit = world.raycast(ctx);

            target = hit.getPos();

            if (!world.isClient) {
                BlockPos blockPos = new BlockPos((int) Math.floor(target.x), (int) Math.floor(target.y), (int) Math.floor(target.z));
                ServerWorld serverWorld = (ServerWorld) world;
                int xpCharge = Math.min(100, player.totalExperience);

                player.addExperience(-xpCharge);

                SculkSpreadManager spreader = SculkSpreadManager.create();
                spreader.spread(blockPos, xpCharge);

                SPREAD_TASKS.add(() -> {
                    spreader.tick(serverWorld, blockPos, serverWorld.getRandom(), true);

                    return spreader.getCursors().isEmpty();
                });
            }

            return true;
        }
    }

    public static class ShriekSkill implements Skill {
        @Override
        public float cost() {
            return 1;
        }

        @Override
        public String name() {
            return "shriek";
        }

        @Override
        public Text desc() {
            return Skill.defDesc(cost(), LIGHT, "Shriek", "Activates all sculk sensors within a 64 block radius.");
        }

        @Override
        public boolean cast(World world, PlayerEntity player) {
            if (!Skill.super.cast(world, player)) {
                return false;
            }

            if (!world.isClient) {
                world.emitGameEvent(Arcanal.ABYSSAL_SHRIEK, player.getPos(), GameEvent.Emitter.of(player));
            }

            return true;
        }
    }
}
