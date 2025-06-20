package net.typho.arcanal.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.typho.arcanal.Arcanal;
import net.typho.arcanal.ability.Ability;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSource.class)
public class AbilityDeathMessageMixin {
    @Shadow
    @Final
    private Entity attacker;

    @Inject(
            method = "getDeathMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getDeathMessage(LivingEntity killed, CallbackInfoReturnable<Text> ci) {
        Entity killer = attacker == null ? killed.getPrimeAdversary() : attacker;

        if (killer instanceof PlayerEntity player) {
            Ability ability = Arcanal.getAbility(player);
            Text text = ability.getDeathMessage(killed, player);

            if (text != null) {
                ci.setReturnValue(text);
            }
        }
    }
}