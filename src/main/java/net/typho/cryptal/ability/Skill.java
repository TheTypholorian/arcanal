package net.typho.cryptal.ability;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.typho.cryptal.Cryptal;
import org.jetbrains.annotations.NotNull;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;

public abstract class Skill {
    public static final Identifier CAST_TO_SERVER_PACKET_ID = new Identifier(Cryptal.MOD_ID, "cast_skill_to_server");
    public static final Identifier CAST_TO_CLIENT_PACKET_ID = new Identifier(Cryptal.MOD_ID, "cast_skill_to_client");

    public Skill() {
    }

    public boolean canCast(PlayerEntity player) {
        return Cryptal.getMana(player) >= cost();
    }

    public abstract float cost();

    public abstract String name();

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

    public abstract static class Flying extends Skill {
        public static final ComponentKey<Skill.Flying.Component> CAN_FLY_COMPONENT = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(Cryptal.MOD_ID, "can_fly"), Skill.Flying.Component.class);

        public abstract WorldParticleBuilder particles();

        public abstract int numParticles();

        public abstract float force();

        @Override
        public float cost() {
            return 0.1f;
        }

        @Override
        public String name() {
            return "flying";
        }

        @Override
        public boolean cast(World world, PlayerEntity player) {
            if (!super.cast(world, player)) {
                return false;
            }

            if (!CAN_FLY_COMPONENT.get(player).canFly()) {
                return false;
            }

            if (!world.isClient) {
                player.setVelocity(player.getVelocity().add(0, force(), 0));
                player.velocityModified = true;
                player.limitFallDistance();
            } else {
                WorldParticleBuilder particles = particles()
                        .setMotion(0, -force(), 0);
                int num = numParticles();

                for (int i = 0; i < num; i++) {
                    particles.setLifetime(40 + (int) (Math.random() * 20))
                            .spawn(world, player.getX(), player.getY(), player.getZ());
                }
            }

            return true;
        }

        public static class Component implements ComponentV3, AutoSyncedComponent, ServerTickingComponent {
            private boolean canFly = false;
            private final LivingEntity parent;

            public Component(LivingEntity parent) {
                this.parent = parent;
            }

            public boolean canFly() {
                return canFly;
            }

            public void setCanFly(boolean canFly) {
                this.canFly = canFly;
                CAN_FLY_COMPONENT.sync(parent);
            }

            @Override
            public void serverTick() {
                if (!canFly) {
                    if (!parent.isOnGround() && parent.fallDistance > 0.1) {
                        setCanFly(true);
                    }
                } else if (parent.isOnGround()) {
                    setCanFly(false);
                }
            }

            @Override
            public void readFromNbt(@NotNull NbtCompound nbt) {
                setCanFly(nbt.getBoolean("canFly"));
            }

            @Override
            public void writeToNbt(@NotNull NbtCompound nbt) {
                nbt.putBoolean("canFly", canFly);
            }
        }
    }
}
