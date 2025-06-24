package net.typho.arcanal.ability;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.typho.arcanal.ArcanalClient;

import java.awt.*;

public class Dimensional implements Ability {
    public static final Color LIGHT = new Color(131, 8, 228), DARK = new Color(39, 30, 61);
    public static final Dimensional INSTANCE = new Dimensional();

    public final Skill[] skills = new Skill[3];

    public Dimensional() {
        skills[0] = new GhastSkill();
        skills[1] = new EndermanSkill();
    }

    @Override
    public String name() {
        return "dimensional";
    }

    @Override
    public Text desc() {
        return Ability.defDesc("Dimensional", LIGHT, skills, "Attacking sets target on fire for 3 seconds. You are immune to fire.");
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

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    public int fireWalkerLevel() {
        return 2;
    }

    public static void freezeLava(LivingEntity entity, World world, BlockPos blockPos, int level) {
        if (entity.isOnGround()) {
            BlockState blockState = Blocks.MAGMA_BLOCK.getDefaultState();
            int i = Math.min(16, 2 + level);
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for (BlockPos blockPos2 : BlockPos.iterate(blockPos.add(-i, -1, -i), blockPos.add(i, -1, i))) {
                if (blockPos2.isWithinDistance(entity.getPos(), i)) {
                    mutable.set(blockPos2.getX(), blockPos2.getY() + 1, blockPos2.getZ());
                    BlockState blockState2 = world.getBlockState(mutable);

                    if (blockState2.isAir()) {
                        BlockState blockState3 = world.getBlockState(blockPos2);

                        if (blockState3 == Blocks.LAVA.getDefaultState()
                                && blockState.canPlaceAt(world, blockPos2)
                                && world.canPlace(blockState, blockPos2, ShapeContext.absent())) {
                            world.setBlockState(blockPos2, blockState);
                        }
                    }
                }
            }
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

    public static class EndermanSkill implements Skill.Missile {
        @Override
        public String name() {
            return "enderman";
        }

        @Override
        public Text desc() {
            return Skill.defDesc(cost(), keybind(), LIGHT, "Enderman", "Teleport in the direction you're looking, with a max distance of 32 blocks.");
        }

        @Override
        public float maxLen() {
            return 32;
        }

        @Override
        public KeyBinding keybind() {
            return ArcanalClient.KEYBINDING_2;
        }

        @Override
        public boolean castServer(ServerWorld world, ServerPlayerEntity player, Vec3d origin, Vec3d dir, Vec3d target) {
            Vec3d last = player.getPos();

            if (player.teleport(target.x, target.y, target.z, true)) {
                world.emitGameEvent(GameEvent.TELEPORT, last, GameEvent.Emitter.of(player));
                world.playSound(null, last.getX(), last.getY(), last.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 1);
                player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1, 1);

                return true;
            }

            return false;
        }
    }
}
