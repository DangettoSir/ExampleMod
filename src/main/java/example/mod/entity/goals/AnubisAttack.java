package example.mod.entity.goals;

import example.mod.entity.bosses.anubis.AnubisEntity;
import example.mod.entity.data.Attack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static example.mod.utils.ParticleHelper.spawnRadiusParticles;

public class AnubisAttack implements Attack {
    private final int duration;
    private final float damage;
    private final boolean isAreaAttack;
    private final boolean isDash;
    private final int[] triggerTicks;
    private final int[] dashRange;
    private final Map<Integer, AttackEffect> effects;

    public AnubisAttack(int duration, float damage, boolean isAreaAttack, boolean isDash, int[] triggerTicks, int[] dashRange) {
        this.duration = duration;
        this.damage = damage;
        this.isAreaAttack = isAreaAttack;
        this.isDash = isDash;
        this.triggerTicks = triggerTicks;
        this.dashRange = dashRange;
        this.effects = initEffects();
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
        if (!shouldExecute(tick)) return;

        rotateTowardsTarget(boss, target);

        if (isDash) {
            executeDash(world, boss, target, source, tick);
        } else {
            executeStandardAttack(world, boss, target, source, tick);
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
        Vec3d dashTargetPos = ((AnubisGoal) boss.getGoalSelector().getGoals().stream()
                .filter(g -> g.getGoal() instanceof AnubisGoal).findFirst().get().getGoal()).getDashTargetPos();

        if (tick == 20) {
            boss.setVelocity(0, 0, 0);
        } else if (tick >= dashRange[0] && tick < dashRange[1]) {
            Vec3d direction = dashTargetPos.subtract(boss.getPos()).normalize();
            double distance = dashTargetPos.distanceTo(boss.getPos());
            if (distance > 0) {
                double speed = distance / (dashRange[1] - tick);
                double height = tick == dashRange[0] ? 3.0 : (target.getY() + 1.5 - boss.getY()) / (dashRange[1] - tick);
                boss.setVelocity(direction.multiply(speed).add(0, height, 0));
                rotateTowardsTarget(boss, target); // Поворот во время рывка
            }
        } else if (tick == dashRange[1]) {
            boss.setPosition(target.getX(), target.getY(), target.getZ());
            for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, boss.getBoundingBox().expand(4.0), e -> e != boss)) {
                entity.damage(source, damage);
            }
        }
    }

    private void executeStandardAttack(ServerWorld world, AnubisEntity boss, LivingEntity target, DamageSource source, int tick) {
        float radius = tick == 189 ? 5.0f : 4.0f;
        if (isAreaAttack && damage > 0) {
            for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, boss.getBoundingBox().expand(radius), e -> e != boss)) {
                entity.damage(source, damage);
            }
        } else if (damage > 0) {
            target.damage(source, damage);
        }
        spawnRadiusParticles(world, boss.getPos(), radius);
    }

    private void rotateTowardsTarget(AnubisEntity boss, LivingEntity target) {
        Vec3d direction = target.getPos().subtract(boss.getPos()).normalize();
        float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float currentYaw = boss.getYaw();
        float yawDelta = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float newYaw = currentYaw + MathHelper.clamp(yawDelta, -20.0f, 20.0f);
        boss.setYaw(newYaw);
        boss.setBodyYaw(newYaw);
        boss.setHeadYaw(newYaw);
        boss.getLookControl().lookAt(target);
    }

    private Map<Integer, AttackEffect> initEffects() {
        Map<Integer, AttackEffect> effects = new HashMap<>();
        effects.put(20, new AttackEffect(ParticleTypes.SMOKE, 30, 2.0, SoundEvents.ENTITY_WITHER_SHOOT, 0.0f));
        effects.put(30, new AttackEffect(isDash ? ParticleTypes.FLAME : ParticleTypes.CRIT, isDash ? 10 : 20, isDash ? 1.0 : 4.0,
                isDash ? null : SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, isDash ? 0.0f : 0.7f, isDash ? 0 : 2.5));
        effects.put(31, new AttackEffect(null, 0, 0.0, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.8f));
        effects.put(28, new AttackEffect(ParticleTypes.SWEEP_ATTACK, 15, 3.0, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 2.0));
        effects.put(24, new AttackEffect(ParticleTypes.SMOKE, 15, 3.0, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f));
        effects.put(36, new AttackEffect(ParticleTypes.SMOKE, 15, 3.0,
                isAreaAttack ? SoundEvents.ENTITY_PLAYER_ATTACK_STRONG : SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                isAreaAttack ? 1.0f : 0.5f));
        effects.put(40, new AttackEffect(null, 0, 0.0, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 0.8f));
        effects.put(69, new AttackEffect(null, 0, 0.0, SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0f));
        effects.put(189, new AttackEffect(null, 0, 0.0, SoundEvents.ENTITY_WITHER_BREAK_BLOCK, 1.5f));
        return effects;
    }
}