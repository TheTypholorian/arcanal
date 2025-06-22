package net.typho.arcanal.ability;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.typho.arcanal.Arcanal;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;

public interface Skill {
    Identifier CAST_TO_SERVER_PACKET_ID = new Identifier(Arcanal.MOD_ID, "cast_skill_to_server");
    Identifier CAST_TO_CLIENT_PACKET_ID = new Identifier(Arcanal.MOD_ID, "cast_skill_to_client");

    default boolean canCast(PlayerEntity player) {
        return Arcanal.getAbility(player).canCast(player, this);
    }

    float cost();

    String name();

    Text desc();

    default void castToServer() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(name());
        ClientPlayNetworking.send(CAST_TO_SERVER_PACKET_ID, buf);
    }

    default boolean cast(World world, PlayerEntity player) {
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

    interface Missile extends Skill {
        @Override
        default float cost() {
            return 3;
        }

        @Override
        default String name() {
            return "missile";
        }

        default float maxLen() {
            return 64;
        }

        default Explosion explosion(World world, PlayerEntity player, Vec3d pos) {
            return new Explosion(world, player, pos.x, pos.y, pos.z, 4, false, Explosion.DestructionType.DESTROY);
        }

        default boolean explosionParticles() {
            return true;
        }

        @Override
        default boolean cast(World world, PlayerEntity player) {
            if (!Skill.super.cast(world, player)) {
                return false;
            }

            Vec3d origin = player.getPos().add(0, player.getStandingEyeHeight(), 0);
            Vec3d look = player.getRotationVector();

            Vec3d target = origin.add(look.multiply(maxLen()));
            RaycastContext ctx = new RaycastContext(origin, target, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player);
            HitResult hit = world.raycast(ctx);

            target = hit.getPos();

            if (!world.isClient) {
                Explosion e = explosion(world, player, target);
                e.collectBlocksAndDamageEntities();
                e.affectWorld(explosionParticles());

                return castServer((ServerWorld) world, (ServerPlayerEntity) player, origin, look, target);
            } else {
                return castClient((ClientWorld) world, (ClientPlayerEntity) player, origin, look, target);
            }
        }

        default boolean castServer(ServerWorld world, ServerPlayerEntity player, Vec3d origin, Vec3d dir, Vec3d target) {
            return true;
        }

        default boolean castClient(ClientWorld world, ClientPlayerEntity player, Vec3d origin, Vec3d dir, Vec3d target) {
            return true;
        }
    }

    interface Shotgun extends Missile {
        WorldParticleBuilder particles();

        int numParticles();

        @Override
        default float cost() {
            return 2;
        }

        @Override
        default String name() {
            return "shotgun";
        }

        @Override
        default float maxLen() {
            return 8;
        }

        @Override
        default boolean castClient(ClientWorld world, ClientPlayerEntity player, Vec3d origin, Vec3d dir, Vec3d target) {
            Vec3d spawn = origin.add(dir);

            WorldParticleBuilder particles = particles()
                    .setMotion(dir.x * 0.5, dir.y * 0.5, dir.z * 0.5);

            for (int i = 0; i < numParticles(); i++) {
                particles.setLifetime(40 + (int) (Math.random() * 20))
                        .spawn(world, spawn.x, spawn.y, spawn.z);
            }

            return true;
        }
    }
}
