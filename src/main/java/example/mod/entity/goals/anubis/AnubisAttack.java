package example.mod.entity.goals.anubis;

import example.mod.entity.bosses.anubis.AnubisEntity;
import example.mod.entity.data.Attack;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.*;

import static example.mod.utils.ParticleHelper.spawnRadiusParticles;

public class AnubisAttack implements Attack {
    private final int duration;
    private final float damage;
    private final boolean isAreaAttack;
    private final boolean isDash;
    private final int[] triggerTicks;
    private final int[] dashRange;
    private final Map<Integer, AttackEffect> effects;
    private Vec3d lastTargetPos;

    public AnubisAttack(int duration, float damage, boolean isAreaAttack, boolean isDash, int[] triggerTicks, int[] dashRange) {
        this.duration = duration;
        this.damage = damage;
        this.isAreaAttack = isAreaAttack;
        this.isDash = isDash;
        this.triggerTicks = triggerTicks;
        this.dashRange = dashRange;
        this.effects = initEffects();
        this.lastTargetPos = null;
    }

    public AnubisAttack(int duration, float damage, boolean isAreaAttack, boolean isDash, int[] triggerTicks) {
        this(duration, damage, isAreaAttack, isDash, triggerTicks, new int[]{0, 0});
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public float getDamage() {
        return damage;
    }

    @Override
    public void execute(ServerWorld world, AnubisEntity boss, LivingEntity target, DamageSource source, int tick) {
        if (tick == 1 && isDash && target != null) {
            lastTargetPos = target.getPos();
            Vec3d direction = lastTargetPos.subtract(boss.getPos()).normalize();
            float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
            boss.setYaw(targetYaw);
            boss.setBodyYaw(targetYaw);
            boss.setNoGravity(true);
            System.out.println("[Anubis Dash] Target position set to: " + lastTargetPos);
        }

        if (!shouldExecute(tick)) return;

        if (isDash) {
            executeDash(world, boss, target, source, tick);
        } else {
            executeStandardAttack(world, boss, source, tick);
        }

        AttackEffect effect = effects.get(tick);
        if (effect != null) {
            effect.apply(world, boss);
        }
    }

    @Override
    public boolean shouldExecute(int tick) {
        if (isDash && dashRange[0] != 0) {
            return Arrays.stream(triggerTicks).anyMatch(t -> t == tick) || (tick >= dashRange[0] && tick <= dashRange[1]);
        }
        return Arrays.stream(triggerTicks).anyMatch(t -> t == tick);
    }

    private void executeDash(ServerWorld world, AnubisEntity boss, LivingEntity target, DamageSource source, int tick) {
        if (lastTargetPos == null || target == null) {
            System.out.println("[Anubis Dash] Dash aborted: lastTargetPos or target is null");
            return;
        }

        if (tick == 52) {
            Vec3d start = boss.getPos();
            Vec3d targetPos = target.getPos();
            Vec3d direction = targetPos.subtract(start).normalize();
            Vec3d hitPos = targetPos.subtract(direction.multiply(1.5));

            damageEntitiesAlongPath(world, boss, source, start, hitPos);

            boss.setPos(hitPos.x, hitPos.y, hitPos.z);
            boss.setNoGravity(false);
            boss.lookAtTarget(target);

            spawnSandVortex(world, start, hitPos);
            world.playSound(null, boss.getBlockPos(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 2.0F, 1.0F);

            boss.setInvulnerable(true);

        } else if (tick == 79) {
            Vec3d attackPos = boss.getPos().add(boss.getRotationVector().multiply(3.0));
            Box attackBox = Box.from(attackPos).expand(2.0);
            for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, attackBox, e -> e != boss)) {
                entity.damage(source, damage);
            }
            spawnRadiusParticles(world, attackPos, 2.0f);
            boss.setVelocity(0, 0, 0);
        }
    }

    private void spawnSandVortex(ServerWorld world, Vec3d start, Vec3d end) {
        ParticleEffect sandEffect = new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, Blocks.SAND.getDefaultState());
        List<Vec3d> increments = getIncrements(start, end, 20);
        for (Vec3d pos : increments) {
            world.spawnParticles(sandEffect, pos.x, pos.y + 1.0, pos.z, 5, 0.2, 0.2, 0.2, 0.05);
        }
        world.spawnParticles(ParticleTypes.FLASH, start.x, start.y + 1.0, start.z, 1, 0.0, 0.0, 0.0, 0.0);
        world.spawnParticles(ParticleTypes.FLASH, end.x, end.y + 1.0, end.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    private List<Vec3d> getIncrements(Vec3d start, Vec3d end, int count) {
        List<Vec3d> result = new ArrayList<>();
        Vec3d diff = end.subtract(start);
        for (int i = 0; i <= count; i++) {
            result.add(start.add(diff.multiply(i / (double) count)));
        }
        return result;
    }

    private void damageEntitiesAlongPath(ServerWorld world, AnubisEntity boss, DamageSource source, Vec3d start, Vec3d end) {
        List<LivingEntity> hitEntities = new ArrayList<>();
        List<Vec3d> increments = getIncrements(start, end, 10);
        for (Vec3d pos : increments) {
            Box hitBox = Box.of(pos, 1.5D, 1.5D, 1.5D);
            for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, hitBox, e -> e != boss && !hitEntities.contains(e))) {
                entity.damage(source, damage);
                Vec3d direction = pos.subtract(entity.getPos()).normalize();
                entity.takeKnockback(0.5F, direction.x, direction.z);
                hitEntities.add(entity);
            }
        }
    }

    private void executeStandardAttack(ServerWorld world, AnubisEntity boss, DamageSource source, int tick) {
        Vec3d attackPos = boss.getPos().add(boss.getRotationVector().multiply(2.0));

        if ((tick == 61 && boss.getState() == AnubisEntity.States.ATTACK_1_P1) ||
                (tick == 36 && boss.getState() == AnubisEntity.States.ATTACK_2_P1) ||
                (tick == 31 && boss.getState() == AnubisEntity.States.ATTACK_4_P2)) {
            Box attackBox = Box.from(attackPos).expand(4.0);
            for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, attackBox, e -> e != boss)) {
                entity.damage(source, damage);
            }
            spawnRadiusParticles(world, attackPos, 4.0f);
        } else if (damage > 0) {
            Vec3d forward = boss.getRotationVector().normalize();
            Box attackBox = new Box(
                    attackPos.x - 0.25, attackPos.y - 0.5, attackPos.z - 0.25,
                    attackPos.x + forward.x * 3.0 + 0.25, attackPos.y + 1.5, attackPos.z + forward.z * 3.0 + 0.25
            );
            for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, attackBox, e -> e != boss)) {
                entity.damage(source, damage);
            }
            spawnRadiusParticles(world, attackPos.add(forward.multiply(1.5)), 0.5f);
        }
    }

    private Map<Integer, AttackEffect> initEffects() {
        Map<Integer, AttackEffect> effects = new HashMap<>();
        effects.put(0, new AttackEffect(ParticleTypes.SMOKE, 30, 2.0,
                SoundEvents.ENTITY_WITHER_SHOOT, 2.0f));
        effects.put(52, new AttackEffect(ParticleTypes.FLAME, 20, 1.0,
                SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 3.0f));
        effects.put(79, new AttackEffect(ParticleTypes.EXPLOSION, 50, 4.0,
                SoundEvents.ENTITY_WITHER_BREAK_BLOCK, 5.0f));
        effects.put(25, new AttackEffect(ParticleTypes.SMOKE, 15, 3.0,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f));
        effects.put(35, new AttackEffect(ParticleTypes.SMOKE, 15, 3.0,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f));
        effects.put(61, new AttackEffect(ParticleTypes.EXPLOSION, 20, 4.0,
                SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 4.0f));
        effects.put(36, new AttackEffect(ParticleTypes.EXPLOSION, 20, 4.0,
                SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 4.0f));
        effects.put(30, new AttackEffect(ParticleTypes.SMOKE, 15, 3.0,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f));
        effects.put(31, new AttackEffect(ParticleTypes.EXPLOSION, 20, 4.0,
                SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, 4.0f));
        effects.put(28, new AttackEffect(ParticleTypes.SWEEP_ATTACK, 15, 3.0,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f));
        effects.put(41, new AttackEffect(ParticleTypes.CRIT, 20, 2.0,
                SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 3.0f));
        effects.put(190, new AttackEffect(ParticleTypes.LARGE_SMOKE, 50, 5.0,
                SoundEvents.ENTITY_WITHER_BREAK_BLOCK, 5.0f));
        return effects;
    }

    public boolean isAreaAttack() {
        return isAreaAttack;
    }
}