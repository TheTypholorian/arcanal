package net.typho.arcanal.ability;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.typho.arcanal.Arcanal;
import org.jetbrains.annotations.NotNull;

public class ManaComponent implements ComponentV3, AutoSyncedComponent, ServerTickingComponent {
    private float mana = 0;
    private final PlayerEntity parent;

    public ManaComponent(PlayerEntity parent) {
        this.parent = parent;
    }

    public float getMana() {
        return mana;
    }

    public void setMana(float mana) {
        this.mana = MathHelper.clamp(mana, 0, 10);
        Arcanal.MANA_COMPONENT.sync(parent);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt) {
        setMana(nbt.getFloat("mana"));
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt) {
        nbt.putFloat("mana", mana);
    }

    @Override
    public void serverTick() {
        setMana(getMana() + 0.05f);
    }
}
