package net.typho.cryptal;

import net.typho.cryptal.ability.Ability;

public interface EntityWithAbility {
    Ability cryptal$getAbility();

    void cryptal$setAbility(Ability ability);
}
