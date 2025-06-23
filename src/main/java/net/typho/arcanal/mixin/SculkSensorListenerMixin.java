package net.typho.arcanal.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import net.typho.arcanal.Arcanal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/block/entity/SculkSensorBlockEntity$VibrationCallback")
public abstract class SculkSensorListenerMixin {
    @Inject(
            method = "accept",
            at = @At("HEAD"),
            cancellable = true
    )
    private void accept(ServerWorld world, BlockPos pos, GameEvent event, Entity sourceEntity, Entity entity, float distance, CallbackInfo ci) {
        if (sourceEntity instanceof PlayerEntity player && Arcanal.getAbility(player).cancelsSculk(event)) {
            ci.cancel();
        }
    }
}
