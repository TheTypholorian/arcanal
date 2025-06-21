package net.typho.arcanal.ability;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.typho.arcanal.Arcanal;
import org.jetbrains.annotations.NotNull;

public interface ManaComponent extends ComponentV3, AutoSyncedComponent, ServerTickingComponent {
    float getMana();

    void setMana(float mana);

    class Impl implements ManaComponent {
        private float mana = 10;
        private final PlayerEntity parent;

        public Impl(PlayerEntity parent) {
            this.parent = parent;
        }

        @Override
        public float getMana() {
            return Arcanal.getAbility(parent).getMana(parent, mana);
        }

        @Override
        public void setMana(float mana) {
            this.mana = Arcanal.getAbility(parent).setMana(parent, mana);
            Arcanal.MANA_COMPONENT.sync(parent);
        }

        @Override
        public void readFromNbt(@NotNull NbtCompound nbt) {
            setMana(nbt.getFloat("mana"));
        }

        @Override
        public void writeToNbt(@NotNull NbtCompound nbt) {
            nbt.putFloat("mana", getMana());
        }

        @Override
        public void serverTick() {
            if (Arcanal.getAbility(parent).regenMana()) {
                setMana(getMana() + 0.05f);
            }
        }
    }
}
