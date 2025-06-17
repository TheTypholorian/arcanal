package net.typho.cryptal.ability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.typho.cryptal.Cryptal;

public abstract class Skill {
    public static final Identifier CAST_PACKET_ID = new Identifier(Cryptal.MOD_ID, "cast_skill");

    public final Ability parent;
    public float timer = cooldown();

    public Skill(Ability parent) {
        this.parent = parent;
    }

    public boolean canCast() {
        return timer <= 0;
    }

    public float untilCanCast() {
        return timer;
    }

    public void tick(float tickDelta) {
        timer -= tickDelta;
    }

    public abstract float cost();

    public abstract float cooldown();

    public abstract String name();

    public void cast(World world, PlayerEntity player) {
        if (!world.isClient) {
            timer = cooldown();
        }
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();

        if (!canCast()) {
            nbt.putFloat("untilCanCast", untilCanCast());
        }

        return nbt;
    }
}
