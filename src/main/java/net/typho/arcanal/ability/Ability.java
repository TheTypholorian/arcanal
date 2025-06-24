package net.typho.arcanal.ability;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.event.GameEvent;
import net.typho.arcanal.Arcanal;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

public interface Ability {
    Map<String, BiFunction<PlayerEntity, NbtCompound, Ability>> ABILITY_MAP = new LinkedHashMap<>();

    static void put(Ability... abilities) {
        for (Ability ability : abilities) {
            ABILITY_MAP.put(ability.name(), (player, nbt) -> ability);
        }
    }

    String name();

    Text desc();

    static MutableText defDesc(String name, Color header, Skill[] skills, String passive) {
        MutableText text = Text.literal("Ability: ").setStyle(Style.EMPTY.withColor(Formatting.WHITE)).append(Text.literal(name).setStyle(Style.EMPTY.withColor(header.getRGB())));

        if (skills != null) {
            for (Skill skill : skills) {
                if (skill != null) {
                    text.append("\n\n").append(skill.desc());
                }
            }
        }

        if (passive != null) {
            text.append(Text.literal("\n\nPassive").setStyle(Style.EMPTY.withColor(header.getRGB())))
                    .append(Text.literal("\n" + passive).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
        }

        return text;
    }

    Skill[] skills(PlayerEntity player);

    default void clientTick(ClientWorld world, ClientPlayerEntity player) {
        for (Skill skill : skills(player)) {
            if (skill != null) {
                while (skill.keybind().wasPressed()) {
                    skill.castToServer();
                }
            }
        }
    }

    default void onAttack(PlayerEntity attacker, Entity target) {
    }

    default void onAttacked(PlayerEntity attacker, PlayerEntity target) {
    }

    default boolean cancelsSculk(GameEvent event) {
        return false;
    }

    default boolean isFireImmune() {
        return false;
    }

    default int fireWalkerLevel() {
        return 0;
    }

    default Skill getSkill(PlayerEntity player, String name) {
        for (Skill skill : skills(player)) {
            if (skill.name().equalsIgnoreCase(name)) {
                return skill;
            }
        }

        return null;
    }

    default Text getDeathMessage(LivingEntity killed, LivingEntity killer) {
        return null;
    }

    default float getDamage(float damage, PlayerEntity attacker, LivingEntity target) {
        return damage;
    }

    default boolean regenMana() {
        return true;
    }

    default float getMana(PlayerEntity parent, float mana) {
        return mana;
    }

    default float setMana(PlayerEntity parent, float mana) {
        return MathHelper.clamp(mana, 0, getMaxMana(parent));
    }

    default float getMaxMana(PlayerEntity parent) {
        return 10;
    }

    default boolean canCast(PlayerEntity player, Skill skill) {
        return Arcanal.getMana(player) >= skill.cost();
    }

    default NbtCompound toNbt(NbtCompound nbt) {
        nbt.putString("ability", name());
        return nbt;
    }

    class None implements Ability {
        public static final None INSTANCE = new None();

        @Override
        public String name() {
            return "none";
        }

        @Override
        public Text desc() {
            return Text.literal("Ability: None\n\nYou have no cool powers.");
        }

        @Override
        public Skill[] skills(PlayerEntity player) {
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
            Arcanal.ABILITY_COMPONENT.sync(parent);
        }

        @Override
        public void readFromNbt(@NotNull NbtCompound nbt) {
            var cons = ABILITY_MAP.get(nbt.getString("ability"));

            if (cons == null) {
                cons = ABILITY_MAP.get("none");
            }

            setAbility(cons.apply(parent, nbt));
        }

        @Override
        public void writeToNbt(@NotNull NbtCompound nbt) {
            ability.toNbt(nbt);
        }
    }
}
