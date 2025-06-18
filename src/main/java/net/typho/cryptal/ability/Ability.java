package net.typho.cryptal.ability;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.typho.cryptal.Cryptal;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public interface Ability {
    Map<String, Ability> ABILITY_MAP = new LinkedHashMap<>();

    static void put(Ability... abilities) {
        for (Ability ability : abilities) {
            ABILITY_MAP.put(ability.name(), ability);
        }
    }

    String name();

    Skill[] skills();

    default Skill majorSkill() {
        return null;
    }

    default Skill minorSkill() {
        return null;
    }

    default Skill passiveSkill() {
        return null;
    }

    default Skill getSkill(String name) {
        for (Skill skill : skills()) {
            if (skill.name().equalsIgnoreCase(name)) {
                return skill;
            }
        }

        return null;
    }

    class None implements Ability {
        public static final None INSTANCE = new None();

        @Override
        public String name() {
            return "none";
        }

        @Override
        public Skill[] skills() {
            return new Skill[0];
        }
    }

    class Component implements ComponentV3, AutoSyncedComponent {
        private Ability ability = None.INSTANCE;
        private final PlayerEntity parent;

        public Component(PlayerEntity parent) {
            this.parent = parent;
        }

        public Ability getAbility() {
            return ability;
        }

        public void setAbility(Ability ability) {
            this.ability = ability == null ? None.INSTANCE : ability;
            Cryptal.ABILITY_COMPONENT.sync(parent);
        }

        @Override
        public void readFromNbt(@NotNull NbtCompound nbt) {
            setAbility(ABILITY_MAP.get(nbt.getString("ability")));
        }

        @Override
        public void writeToNbt(@NotNull NbtCompound nbt) {
            nbt.putString("ability", ability.name());
        }
    }
}
