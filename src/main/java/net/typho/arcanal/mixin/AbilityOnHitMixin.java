package net.typho.arcanal.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.typho.arcanal.Arcanal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class AbilityOnHitMixin {
    @Inject(
            method = "onAttacking",
            at = @At("HEAD")
    )
    private void onAttack(Entity target, CallbackInfo ci) {
        LivingEntity source = (LivingEntity) (Object) this;

        if (source instanceof PlayerEntity player) {
            Arcanal.getAbility(player).onAttack(player, target);
        }
    }
}
