package net.typho.arcanal.ability;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.event.GameEvent;
import net.typho.arcanal.Arcanal;

import java.util.Objects;

public class Sacrificial implements Ability {
    public Ability copy = Astral.INSTANCE;

    public Sacrificial(PlayerEntity parent, NbtCompound nbt) {
        if (nbt.contains("copy")) {
            NbtCompound copyData = nbt.getCompound("copy");
            copy = ABILITY_MAP.get(copyData.getString("ability")).apply(parent, copyData);
        }
    }

    @Override
    public String name() {
        return "sacrificial";
    }

    @Override
    public Text desc() {
        return null;
    }

    @Override
    public Skill[] skills(PlayerEntity player) {
        return copy.skills(player);
    }

    @Override
    public void clientTick(ClientWorld world, ClientPlayerEntity player) {
        copy.clientTick(world, player);
    }

    @Override
    public void onAttack(PlayerEntity attacker, Entity target) {
        copy.onAttack(attacker, target);
    }

    @Override
    public void onAttacked(PlayerEntity attacker, PlayerEntity target) {
        Ability ability = Arcanal.getAbility(attacker);

        if (!(ability instanceof None)) {
            copy = ability;
            Arcanal.ABILITY_COMPONENT.sync(target);
        }

        copy.onAttacked(attacker, target);
    }

    @Override
    public Skill getSkill(PlayerEntity player, String name) {
        return copy.getSkill(player, name);
    }

    @Override
    public Text getDeathMessage(LivingEntity killed, LivingEntity killer) {
        return copy.getDeathMessage(killed, killer);
    }

    @Override
    public float getDamage(float damage, PlayerEntity attacker, LivingEntity target) {
        return copy.getDamage(damage, attacker, target);
    }

    @Override
    public boolean cancelsSculk(GameEvent event) {
        return copy.cancelsSculk(event);
    }

    @Override
    public boolean regenMana() {
        return false;
    }

    @Override
    public float getMana(PlayerEntity parent, float mana) {
        return parent.getHealth();
    }

    @Override
    public float setMana(PlayerEntity parent, float mana) {
        parent.setHealth(mana);

        if (parent.isDead() && !parent.getWorld().isClient) {
            Objects.requireNonNull(parent.getWorld().getServer()).getPlayerManager().broadcast(parent.getDisplayName().copy().append(Text.translatable("death.arcanal.sacrificial")), false);
        }

        return parent.getHealth();
    }

    @Override
    public float getMaxMana(PlayerEntity parent) {
        return parent.getMaxHealth();
    }

    @Override
    public boolean canCast(PlayerEntity player, Skill skill) {
        return Arcanal.getMana(player) >= skill.cost() - 2;
    }

    @Override
    public NbtCompound toNbt(NbtCompound nbt) {
        Ability.super.toNbt(nbt);
        nbt.put("copy", copy.toNbt(new NbtCompound()));
        return nbt;
    }
}
