package net.typho.cryptal.ability.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.typho.cryptal.Cryptal;
import net.typho.cryptal.EntityWithAbility;
import net.typho.cryptal.ability.Ability;

public class AbilitySyncC2SPacket {
    public static final Identifier ID = new Identifier(Cryptal.MOD_ID, "sync_ability_client");

    public static void send(Ability ability) {
        PacketByteBuf buf = PacketByteBufs.create();
        ability.toNbt(new NbtCompound()).write(buf);
        ClientPlayNetworking.send(ID, buf);
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
            NbtCompound data = buf.readNbt();
            client.execute(() -> {
                Ability ability = Ability.fromNbt(data);
                if (client.player instanceof EntityWithAbility withAbility) {
                    withAbility.cryptal$setAbility(ability);
                }
            });
        });
    }
}
