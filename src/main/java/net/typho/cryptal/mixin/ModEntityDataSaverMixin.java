package net.typho.cryptal.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.typho.cryptal.Cryptal;
import net.typho.cryptal.EntityWithAbility;
import net.typho.cryptal.ability.Ability;
import net.typho.cryptal.ability.AbilitySyncS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class ModEntityDataSaverMixin implements EntityWithAbility {
    @Unique
    private Ability ability = new Ability.None();

    @Override
    public Ability cryptal$getAbility() {
        return ability;
    }

    @Override
    public void cryptal$setAbility(Ability ability) {
        this.ability = ability == null ? new Ability.None() : ability;

        if ((Object) this instanceof ServerPlayerEntity serverPlayer) {
            PacketByteBuf buf = PacketByteBufs.create();
            AbilitySyncS2CPacket.write(buf, this.ability);
            ServerPlayNetworking.send(serverPlayer, AbilitySyncPackets.SYNC_TO_CLIENT, buf);
        } else if (FMLEnvironment.dist.isClient() && MinecraftClient.getInstance().getNetworkHandler() != null) {
            PacketByteBuf buf = PacketByteBufs.create();
            AbilitySyncPackets.write(buf, this.ability);
            ClientPlayNetworking.send(AbilitySyncPackets.SYNC_TO_SERVER, buf);
        }
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    protected void injectWriteMethod(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> info) {
        nbt.put(Cryptal.MOD_ID + ".ability", cryptal$getAbility().toNbt(new NbtCompound()));
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    protected void injectReadMethod(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains(Cryptal.MOD_ID + ".ability", NbtElement.COMPOUND_TYPE)) {
            Ability ability = Ability.fromNbt(nbt.getCompound(Cryptal.MOD_ID + ".ability"));
            this.ability = ability == null ? new Ability.None() : ability;
        }
    }
}
