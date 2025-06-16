package net.typho.elvin.ability;

import net.minecraft.nbt.NbtCompound;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public interface Ability {
    Map<String, Function<NbtCompound, Ability>> FROM_NBT_MAP = new LinkedHashMap<>();

    String name();

    default NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", name());
        return nbt;
    }

    static Ability fromNbt(NbtCompound nbt) {
        return Objects.requireNonNull(FROM_NBT_MAP.get(nbt.getString("name")), () -> "No ability of name " + nbt.getString("name")).apply(nbt);
    }
}
