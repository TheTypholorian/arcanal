package net.typho.cryptal.ability;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.typho.cryptal.Cryptal;
import net.typho.cryptal.EntityWithAbility;

public class AbilitySyncS2CPacket {
    public static final Identifier ID = new Identifier(Cryptal.MOD_ID, "sync_ability_server");

    public static void send(ServerPlayerEntity player, Ability ability) {
        PacketByteBuf buf = PacketByteBufs.create();
        ability.toNbt(new NbtCompound()).write(buf);
        ServerPlayNetworking.send(player, ID, buf);
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            NbtCompound data = buf.readNbt();

            server.execute(() -> {
                assert data != null;
                Ability ability = Ability.fromNbt(data);

                if (player instanceof EntityWithAbility withAbility) {
                    withAbility.cryptal$setAbility(ability);
                    send(player, ability);
                }
            });
        });
    }
}
