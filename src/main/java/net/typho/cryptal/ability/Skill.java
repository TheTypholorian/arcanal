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

    public Skill(Ability parent, NbtCompound nbt) {
        this(parent);

        NbtCompound data = nbt.getCompound(name());

        if (data != null) {
            if (data.contains("timer")) {
                timer = data.getFloat("timer");
            }
        }
    }

    public boolean canCast() {
        //return timer <= 0;
        return true;
    }

    public float untilCanCast() {
        return timer;
    }

    public void tick(float tickDelta) {
        timer = Math.max(0, timer - tickDelta);
    }

    public abstract float cost();

    public abstract float cooldown();

    public abstract String name();

    public boolean cast(World world, PlayerEntity player) {
        if (!canCast()) {
            return false;
        }

        if (!world.isClient) {
            timer = cooldown();
        }

        return true;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();

        nbt.putFloat("timer", untilCanCast());

        return nbt;
    }
}
