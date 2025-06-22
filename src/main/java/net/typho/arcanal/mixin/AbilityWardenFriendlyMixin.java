package net.typho.arcanal.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.typho.arcanal.Arcanal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WardenEntity.class)
public abstract class AbilityWardenFriendlyMixin extends LivingEntity {
    protected AbilityWardenFriendlyMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        if (target instanceof PlayerEntity player) {
            if (Arcanal.getAbility(player).cancelsSculk(null)) {
                return false;
            }
        }

        return super.canTarget(target);
    }

    @Inject(method = "getTarget", at = @At("RETURN"), cancellable = true)
    private void getTarget(CallbackInfoReturnable<LivingEntity> cir) {
        LivingEntity target = cir.getReturnValue();

        if (target != null && !canTarget(target)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "isValidTarget", at = @At("RETURN"), cancellable = true)
    private void isValidTarget(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof LivingEntity living && !canTarget(living)) {
            cir.setReturnValue(false);
        }
    }
}
