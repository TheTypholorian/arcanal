package net.typho.arcanal.ability;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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

    public boolean cast(World world, PlayerEntity player) {
        if (!canCast(player)) {
            return false;
        }

        if (world.isClient) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(name());
            ClientPlayNetworking.send(CAST_TO_SERVER_PACKET_ID, buf);
        } else {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(name());
            buf.writeInt(player.getId());

            for (PlayerEntity send : world.getPlayers()) {
                ServerPlayNetworking.send((ServerPlayerEntity) send, CAST_TO_CLIENT_PACKET_ID, buf);
            }
        }

        return true;
    }

    public abstract static class Flamethrower extends Skill {
        public abstract WorldParticleBuilder particles();

        public abstract int numParticles();

        public abstract DamageSource damageSource(World world, PlayerEntity player);

        public abstract int fireSeconds();

        public abstract int fireDamage();

        @Override
        public float cost() {
            return 1f;
        }

        @Override
        public String name() {
            return "flamethrower";
        }

        @Override
        public boolean cast(World world, PlayerEntity player) {
            if (!super.cast(world, player)) {
                return false;
            }

            Vec3d origin = player.getPos().add(0, player.getStandingEyeHeight(), 0);
            Vec3d look = player.getRotationVector();
            float len = 64;

            HitResult hit = Arcanal.raycast(world, player, len);

            if (hit instanceof BlockHitResult block) {
                BlockPos firePos = block.getBlockPos().offset(block.getSide());

                if (world.isAir(firePos) && !world.isAir(firePos.down())) {
                    world.setBlockState(
                            firePos,
                            Blocks.FIRE.getDefaultState(),
                            Block.NOTIFY_ALL
                    );
                }
            } else if (hit instanceof EntityHitResult entity) {
                Entity e = entity.getEntity();

                e.setOnFireFor(fireSeconds());

                if (e.damage(damageSource(world, player), fireDamage())) {
                    e.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4f, 2f + world.random.nextFloat() * 0.4f);
                }
            }

            if (world.isClient) {
                player.playSound(
                        SoundEvents.ITEM_FIRECHARGE_USE,
                        SoundCategory.BLOCKS,
                        3f,
                        (1f + (world.random.nextFloat() - world.random.nextFloat()) * 0.2f) * 0.7f
                );

                Vec3d spawn = origin.add(look);

                WorldParticleBuilder particles = particles()
                        .setMotion(look.x, look.y, look.z);

                for (int i = 0; i < numParticles(); i++) {
                    particles.setLifetime(40 + (int) (Math.random() * 20))
                            .spawn(world, spawn.x, spawn.y, spawn.z);
                }
            }

            return true;
        }
    }
}
