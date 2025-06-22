package net.typho.arcanal.ability;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.typho.arcanal.ArcanalClient;

public class Abyssal implements Ability {
    public static final Abyssal INSTANCE = new Abyssal();
    public final Skill[] skills = new Skill[3];

    public Abyssal() {
        skills[0] = new SonicBoomSkill();
    }

    @Override
    public String name() {
        return "abyssal";
    }

    @Override
    public Skill[] skills(PlayerEntity player) {
        return skills;
    }

    @Override
    public boolean cancelsSculk() {
        return true;
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
        public Explosion explosion(World world, PlayerEntity player, Vec3d pos) {
            return new Astral.Shockwave(world, player, pos.x, pos.y, pos.z, 10, false, Explosion.DestructionType.KEEP);
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
}
