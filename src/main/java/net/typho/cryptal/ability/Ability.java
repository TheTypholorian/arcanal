package net.typho.cryptal.ability;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.typho.cryptal.Cryptal;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public interface Ability {
    Map<String, Function<NbtCompound, Ability>> FROM_NBT_MAP = new LinkedHashMap<>();

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
        private Ability ability = new None();
        private final PlayerEntity parent;

        public Component(PlayerEntity parent) {
            this.parent = parent;
        }

        public Ability getAbility() {
            return ability;
        }

        public void setAbility(Ability ability) {
            this.ability = ability;
            Cryptal.ABILITY_COMPONENT.sync(parent);
        }

        @Override
        public void readFromNbt(@NotNull NbtCompound nbt) {
            setAbility(fromNbt(nbt));
        }

        @Override
        public void writeToNbt(@NotNull NbtCompound nbt) {
            ability.toNbt(nbt);
        }
    }
}
