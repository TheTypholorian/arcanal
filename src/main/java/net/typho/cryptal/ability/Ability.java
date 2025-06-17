package net.typho.cryptal.ability;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import oshi.driver.windows.wmi.OhmHardware;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public interface Ability {
    Map<String, Function<NbtCompound, Ability>> FROM_NBT_MAP = new LinkedHashMap<>();

    PlayerEntity parent();

    void parent(PlayerEntity player);

    String name();

    Skill[] skills();

    default Skill getSkill(String name) {
        for (Skill skill : skills()) {
            if (skill.name().equalsIgnoreCase(name)) {
                return skill;
            }
        }

        return null;
    }

    default NbtCompound toNbt(NbtCompound nbt) {
        nbt.putString("name", name());

        NbtCompound skills = new NbtCompound();

        for (Skill skill : skills()) {
            if (skill != null) {
                skills.put(skill.name(), skill.toNbt());
            }
        }

        return nbt;
    }

    static Ability fromNbt(NbtCompound nbt) {
        return Objects.requireNonNull(FROM_NBT_MAP.get(nbt.getString("name")), () -> "No ability of name " + nbt.getString("name")).apply(nbt);
    }

    class None implements Ability {
        private PlayerEntity parent;

        @Override
        public PlayerEntity parent() {
            return parent;
        }

        @Override
        public void parent(PlayerEntity player) {
            parent = player;
        }

        @Override
        public String name() {
            return "none";
        }

        @Override
        public Skill[] skills() {
            return new Skill[0];
        }
    }
}
