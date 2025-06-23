package net.typho.arcanal.ability;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.awt.*;

public class Pestal implements Ability {
    public static final Color LIGHT = new Color(57, 76, 58), DARK = new Color(19, 25, 19);
    public static final Pestal INSTANCE = new Pestal();
    public final Skill[] skills = new Skill[1];

    public Pestal() {
        skills[0] = new ReapSkill();
    }

    @Override
    public String name() {
        return "pestal";
    }

    @Override
    public Text desc() {
        return Ability.defDesc("Pestal", LIGHT, skills, "Inflicts hit entities with Poison II for 5 seconds");
    }

    @Override
    public Skill[] skills(PlayerEntity player) {
        return skills;
    }

    @Override
    public void onAttack(PlayerEntity attacker, Entity target) {
        if (target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1));
        }
    }

    public static class ReapSkill implements Skill {
        @Override
        public float cost() {
            return 10;
        }

        @Override
        public String name() {
            return "";
        }

        @Override
        public Text desc() {
            return null;
        }

        @Override
        public KeyBinding keybind() {
            return null;
        }
    }
}
