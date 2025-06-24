package net.typho.arcanal.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.typho.arcanal.Arcanal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(
            method = "isFireImmune",
            at = @At("HEAD"),
            cancellable = true
    )
    private void isFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if ((Entity) (Object) this instanceof PlayerEntity player && Arcanal.getAbility(player).isFireImmune()) {
            cir.setReturnValue(true);
        }
    }
}
