package net.typho.arcanal.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.Vibrations;
import net.typho.arcanal.Arcanal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Vibrations.VibrationListener.class)
public class AbilityTriggerSculkMixin {
    @Inject(
            method = "listen(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/world/event/GameEvent$Emitter;Lnet/minecraft/util/math/Vec3d;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onListen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d pos, CallbackInfoReturnable<Boolean> cir) {
        if (emitter.sourceEntity() instanceof PlayerEntity player && Arcanal.getAbility(player).cancelsSculk(event)) {
            cir.setReturnValue(false);
        }
    }
}
