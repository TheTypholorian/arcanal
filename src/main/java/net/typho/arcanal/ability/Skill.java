package net.typho.arcanal.ability;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.typho.arcanal.Arcanal;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;

public abstract class Skill {
    public static final Identifier CAST_TO_SERVER_PACKET_ID = new Identifier(Arcanal.MOD_ID, "cast_skill_to_server");
    public static final Identifier CAST_TO_CLIENT_PACKET_ID = new Identifier(Arcanal.MOD_ID, "cast_skill_to_client");

    public Skill() {
    }

    public boolean canCast(PlayerEntity player) {
        return Arcanal.getMana(player) >= cost();
    }

    public abstract float cost();

    public abstract String name();

    public void castToServer() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(name());
        ClientPlayNetworking.send(CAST_TO_SERVER_PACKET_ID, buf);
    }

    public boolean cast(World world, PlayerEntity player) {
        if (!canCast(player)) {
            return false;
        }

        if (!world.isClient) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(name());
            buf.writeInt(player.getId());

            for (PlayerEntity send : world.getPlayers()) {
                ServerPlayNetworking.send((ServerPlayerEntity) send, CAST_TO_CLIENT_PACKET_ID, buf);
            }
        }

        return true;
    }

    public abstract static class Shotgun extends Skill {
        public abstract WorldParticleBuilder particles();

        public abstract int numParticles();

        @Override
        public float cost() {
            return 2f;
        }

        @Override
        public String name() {
            return "shotgun";
        }

        @Override
        public boolean cast(World world, PlayerEntity player) {
            if (!super.cast(world, player)) {
                return false;
            }

            Vec3d origin = player.getPos().add(0, player.getStandingEyeHeight(), 0);
            Vec3d look = player.getRotationVector();
            float len = 8;

            if (world.isClient) {
                Vec3d spawn = origin.add(look);
                look = look.multiply(0.5);

                WorldParticleBuilder particles = particles()
                        .setMotion(look.x, look.y, look.z);

                for (int i = 0; i < numParticles(); i++) {
                    particles.setLifetime(40 + (int) (Math.random() * 20))
                            .spawn(world, spawn.x, spawn.y, spawn.z);
                }
            } else {
                HitResult hit = Arcanal.raycast(world, player, len);

                Explosion explosion = new Explosion(world, player, hit.getPos().x, hit.getPos().y, hit.getPos().z, 3, true, Explosion.DestructionType.KEEP);
                explosion.collectBlocksAndDamageEntities();
                explosion.affectWorld(false);
            }

            return true;
        }
    }
}
