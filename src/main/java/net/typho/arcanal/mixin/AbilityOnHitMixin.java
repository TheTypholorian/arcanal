package net.typho.arcanal.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.typho.arcanal.Arcanal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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
            if (target instanceof PlayerEntity tp) {
                Arcanal.getAbility(player).onAttacked(player, tp);
            }

            Arcanal.getAbility(player).onAttack(player, target);
        }
    }

    @ModifyArg(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"
            ),
            index = 1
    )
    private float adjustDamageArg(DamageSource src, float damage) {
        LivingEntity target = (LivingEntity) (Object) this;

        if (src.getAttacker() instanceof PlayerEntity attacker) {
            return Arcanal.getAbility(attacker).getDamage(damage, attacker, target);
        }

        return damage;
    }
}
