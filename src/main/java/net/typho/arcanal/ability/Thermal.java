package net.typho.arcanal.ability;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.typho.arcanal.ArcanalClient;

import java.awt.*;

public class Thermal implements Ability {
    public static final Color LIGHT = new Color(236, 187, 80), DARK = new Color(199, 66, 9);
    public static final Thermal INSTANCE = new Thermal();

    public final Skill[] skills = new Skill[3];

    public Thermal() {
        skills[0] = new GhastSkill();
    }

    @Override
    public String name() {
        return "thermal";
    }

    @Override
    public Text desc() {
        return Ability.defDesc("Thermal", LIGHT, skills, "Attacking sets target on fire for 3 seconds.");
    }

    @Override
    public Skill[] skills(PlayerEntity player) {
        return skills;
    }

    @Override
    public void onAttack(PlayerEntity attacker, Entity target) {
        if (target instanceof LivingEntity living) {
            living.setOnFireFor(3);
        }
    }

    public static class GhastSkill implements Skill {
        @Override
        public float cost() {
            return 10;
        }

        @Override
        public String name() {
            return "ghast";
        }

        @Override
        public Text desc() {
            return Skill.defDesc(cost(), keybind(), LIGHT, "Ghast", "Shoots a ghast fireball in the direction you're looking");
        }

        @Override
        public KeyBinding keybind() {
            return ArcanalClient.KEYBINDING_1;
        }

        @Override
        public boolean cast(World world, PlayerEntity player) {
            if (!Skill.super.cast(world, player)) {
                return false;
            }

            if (!world.isClient) {
                Vec3d look = player.getRotationVector();

                FireballEntity fireball = new FireballEntity(world, player, look.x, look.y, look.z, 4);
                fireball.setPosition(player.getEyePos());
                world.spawnEntity(fireball);
            }

            return true;
        }
    }
}
