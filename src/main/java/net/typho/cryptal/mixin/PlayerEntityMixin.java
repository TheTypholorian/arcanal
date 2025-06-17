package net.typho.cryptal.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.typho.cryptal.Cryptal;
import net.typho.cryptal.ability.Ability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initTrackedAbility(CallbackInfo ci) {
        ((PlayerEntity)(Object) this).getDataTracker().startTracking(Cryptal.ABILITY, new Ability.None().toNbt(new NbtCompound()));
    }
}
