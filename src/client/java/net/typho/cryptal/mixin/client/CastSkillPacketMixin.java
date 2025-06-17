package net.typho.cryptal.mixin.client;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.World;
import net.typho.cryptal.ability.Skill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Skill.class)
public class CastSkillPacketMixin {
    @Inject(method = "cast", at = @At("TAIL"))
    private void onCast(World world, PlayerEntity player, CallbackInfo ci) {
        if (world.isClient) {
            Skill skill = (Skill) (Object) this;
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

            buf.writeString(skill.parent.name());
            buf.writeString(skill.name());

            ClientPlayNetworking.send(Skill.CAST_PACKET_ID, buf);
        }
    }
}