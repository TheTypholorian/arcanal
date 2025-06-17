package net.typho.cryptal.ability;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.typho.cryptal.Cryptal;

public class ManaRegen implements ServerTickEvents.StartTick {
    @Override
    public void onStartTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            EntityAttributeInstance mana = player.getAttributeInstance(Cryptal.MANA_ATTRIBUTE);

            if (mana != null && mana.getAttribute() instanceof ClampedEntityAttribute clamp) {
                mana.setBaseValue(clamp.clamp(mana.getBaseValue() + 0.05));
            }
        }
    }
}
