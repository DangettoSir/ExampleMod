package example.mod.entity.goals;

import example.mod.entity.bosses.anubis.AnubisEntity;
import example.mod.entity.data.Attack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.EnumMap;
import java.util.EnumSet;

public class AnubisGoal extends MeleeAttackGoal {
    private static final double MELEE_RANGE_SQ = 16.0; // 4 блока в квадрате
    private static final double DASH_RANGE_SQ = 324.0; // 18 блоков в квадрате
    private static final int COOLDOWN_TICKS = 20;

    private final AnubisEntity boss;
    private int attackCooldown = COOLDOWN_TICKS;
    private int attackStatus;
    private Vec3d dashTargetPos;
    private final EnumMap<AnubisEntity.States, Attack> attacks = new EnumMap<>(AnubisEntity.States.class);

    public AnubisGoal(AnubisEntity boss) {
        super(boss, 0.4D, true);
        this.boss = boss;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        initAttacks();
    }

    private void initAttacks() {
        attacks.put(AnubisEntity.States.DASH, new AnubisAttack(85, 6.0f, true, true, new int[]{20}, new int[]{30, 69}));
        attacks.put(AnubisEntity.States.ATTACK_1, new AnubisAttack(80, 8.0f, true, false, new int[]{24, 36}));
        attacks.put(AnubisEntity.States.ATTACK_2, new AnubisAttack(60, 10.0f, true, false, new int[]{36}));
        attacks.put(AnubisEntity.States.ATTACK_3, new AnubisAttack(60, 6.0f, false, false, new int[]{30}));
        attacks.put(AnubisEntity.States.ATTACK_4, new AnubisAttack(112, 9.0f, false, false, new int[]{31}));
        attacks.put(AnubisEntity.States.ATTACK_5, new AnubisAttack(60, 7.0f, false, false, new int[]{28}));
        attacks.put(AnubisEntity.States.ATTACK_6, new AnubisAttack(52, 7.0f, true, false, new int[]{40}));
        attacks.put(AnubisEntity.States.ATTACK_7, new AnubisAttack(190, 0.0f, true, false, new int[]{189}));
    }

    @Override
    public void start() {
        super.start();
        boss.setAttacking(true);
    }

    @Override
    public void stop() {
        super.stop();
        boss.setAttacking(false);
        reset();
    }

    private void reset() {
        attackStatus = 0;
        attackCooldown = COOLDOWN_TICKS;
        boss.setState(AnubisEntity.States.IDLE);
        dashTargetPos = null;
    }

    @Override
    public void tick() {
        if (boss.isDead()) return;

        attackCooldown--;
        attackStatus++;

        LivingEntity target = boss.getTarget();
        if (target == null) {
            boss.setState(AnubisEntity.States.IDLE);
            reset();
            return;
        }

        // Постоянно поворачиваем Анубиса к цели
        rotateTowardsTarget(target);

        AnubisEntity.States state = boss.getState();
        if (state != AnubisEntity.States.IDLE && state != AnubisEntity.States.DEATH) {
            Attack attack = attacks.get(state);
            attack.execute((ServerWorld) boss.getWorld(), boss, target, boss.getWorld().getDamageSources().mobAttack(boss), attackStatus);
            if (attackStatus >= attack.getDuration()) reset();
            return;
        }

        if (attackCooldown <= 0 && state == AnubisEntity.States.IDLE) {
            selectNextAction(target);
            attackStatus = 0;
        }
    }

    private void selectNextAction(LivingEntity target) {
        double distanceSq = boss.squaredDistanceTo(target);
        if (distanceSq <= MELEE_RANGE_SQ) {
            boss.setState(selectMeleeAttack());
        } else if (distanceSq >= DASH_RANGE_SQ) {
            boss.setState(AnubisEntity.States.DASH);
            dashTargetPos = target.getPos();
        } else {
            moveTowardsTarget(target);
        }
    }

    private AnubisEntity.States selectMeleeAttack() {
        int phase = boss.getPhase();
        int rand = boss.getRandom().nextInt(phase == 1 ? 3 : 4);
        return phase == 1 ?
                rand == 0 ? AnubisEntity.States.ATTACK_1 :
                        rand == 1 ? AnubisEntity.States.ATTACK_3 : AnubisEntity.States.ATTACK_6 :
                rand == 0 ? AnubisEntity.States.ATTACK_1 :
                        rand == 1 ? AnubisEntity.States.ATTACK_2 :
                                rand == 2 ? AnubisEntity.States.ATTACK_4 : AnubisEntity.States.ATTACK_5;
    }

    private void moveTowardsTarget(LivingEntity target) {
        Vec3d direction = target.getPos().subtract(boss.getPos()).normalize();
        boss.setVelocity(direction.multiply(0.6).add(0, boss.getVelocity().y, 0));
        rotateTowardsTarget(target);
    }

    private void rotateTowardsTarget(LivingEntity target) {
        Vec3d direction = target.getPos().subtract(boss.getPos()).normalize();
        float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float currentYaw = boss.getYaw();
        float yawDelta = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float newYaw = currentYaw + MathHelper.clamp(yawDelta, -10.0f, 10.0f);
        boss.setYaw(newYaw);
        boss.setBodyYaw(newYaw);
        boss.setHeadYaw(newYaw); // Синхронизируем голову с телом
        boss.getLookControl().lookAt(target);
    }

    public Vec3d getDashTargetPos() {
        return dashTargetPos;
    }
}