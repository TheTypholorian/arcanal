package net.typho.elvin.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.typho.elvin.Elvin;

public class AstralKinesisEffect extends StatusEffect {
    public static final AstralKinesisEffect INSTANCE = Registry.register(Registries.STATUS_EFFECT, new Identifier(Elvin.MOD_ID, "astralkinesis"), new AstralKinesisEffect());

    public AstralKinesisEffect() {
        super(StatusEffectCategory.NEUTRAL, Elvin.ASTRAL_BRIGHT.getRGB());
    }
}
