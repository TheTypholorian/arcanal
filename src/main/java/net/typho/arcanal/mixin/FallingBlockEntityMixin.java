package net.typho.arcanal.mixin;

import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {
    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/FallingBlockEntity;discard()V",
                    ordinal = 3
            ),
            cancellable = true
    )
    private void onDestroyedOnLandingReplace(CallbackInfo ci) {
        FallingBlockEntity block = (FallingBlockEntity) (Object) this;
        block.setVelocity(new Vec3d(Math.random() * 0.5 - 0.25, Math.random() * 0.5 - 0.25, Math.random() * 0.5 - 0.25));
        ci.cancel();
    }
}
