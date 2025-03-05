package example.mod.entity.goals.anubis;

import example.mod.entity.bosses.anubis.AnubisEntity;
import example.mod.entity.data.Attack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.function.Supplier;

public class AnubisGoal extends MeleeAttackGoal {
    private static final double MELEE_RANGE_SQ = 9.0;
    private static final double DASH_RANGE_SQ = 324.0;
    private static final int COOLDOWN_TICKS = 20;

    private final AnubisEntity boss;
    private int attackCooldown = COOLDOWN_TICKS;
    private int attackStatus;
    private Vec3d dashTargetPos;
    private final EnumMap<AnubisEntity.States, Supplier<Attack>> attackSuppliers = new EnumMap<>(AnubisEntity.States.class);
    private final EnumMap<AnubisEntity.States, Attack> activeAttacks = new EnumMap<>(AnubisEntity.States.class);

    public AnubisGoal(AnubisEntity boss) {
        super(boss, 0.4D, true);
        this.boss = boss;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        initAttacks();
    }

    private void initAttacks() {
        attackSuppliers.put(AnubisEntity.States.DASH_P1, () ->
                new AnubisAttack(85, 6.0f, true, true, new int[]{52}, new int[]{52, 79}));
        attackSuppliers.put(AnubisEntity.States.ATTACK_1_P1, () ->
                new AnubisAttack(80, 8.0f, true, false, new int[]{25, 35, 61}));
        attackSuppliers.put(AnubisEntity.States.ATTACK_2_P1, () ->
                new AnubisAttack(60, 10.0f, true, false, new int[]{36}));
        attackSuppliers.put(AnubisEntity.States.ATTACK_3_P1, () ->
                new AnubisAttack(60, 6.0f, false, false, new int[]{30}));
        attackSuppliers.put(AnubisEntity.States.DASH_P2, () ->
                new AnubisAttack(85, 6.0f, true, true, new int[]{52}, new int[]{52, 79}));
        attackSuppliers.put(AnubisEntity.States.ATTACK_4_P2, () ->
                new AnubisAttack(112, 9.0f, true, false, new int[]{31}));
        attackSuppliers.put(AnubisEntity.States.ATTACK_5_P2, () ->
                new AnubisAttack(60, 7.0f, false, false, new int[]{28}));
        attackSuppliers.put(AnubisEntity.States.ATTACK_6_P2, () ->
                new AnubisAttack(52, 7.0f, true, false, new int[]{41}));
        attackSuppliers.put(AnubisEntity.States.ATTACK_7_P2, () ->
                new AnubisAttack(190, 0.0f, false, false, new int[]{190}));
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
        activeAttacks.clear();
        boss.setVelocity(0, boss.getVelocity().y, 0);
        boss.setNoGravity(false);
        boss.setInvulnerable(false);
    }

    @Override
    public void tick() {
        if (boss.isDead()) return;

        attackCooldown--;
        attackStatus++;

        LivingEntity target = boss.getTarget();
        if (target == null) {
            reset();
            return;
        }

        AnubisEntity.States state = boss.getState();
        if (state != AnubisEntity.States.IDLE && state != AnubisEntity.States.DEATH) {
            Attack attack = activeAttacks.computeIfAbsent(state, s -> attackSuppliers.get(s).get());
            attack.execute((ServerWorld) boss.getWorld(), boss, target, boss.getWorld().getDamageSources().mobAttack(boss), attackStatus);
            if (state != AnubisEntity.States.DASH_P1 && state != AnubisEntity.States.DASH_P2) {
                boss.setVelocity(0, boss.getVelocity().y, 0);
            }
            if (attackStatus >= attack.getDuration()) {
                reset();
                boss.lookAtTarget(target);
            }
            return;
        }

        if (state == AnubisEntity.States.IDLE) {
            boss.lookAtTarget(target);
            boss.setBodyYaw(boss.getYaw());
        }

        if (attackCooldown <= 0 && state == AnubisEntity.States.IDLE) {
            selectNextAction(target);
            attackStatus = 0;
        } else if (state == AnubisEntity.States.IDLE) {
            moveTowardsTarget(target);
        }
    }

    private void selectNextAction(LivingEntity target) {
        double distanceSq = boss.squaredDistanceTo(target);
        if (distanceSq <= MELEE_RANGE_SQ) {
            boss.setState(selectMeleeAttack());
        } else if (distanceSq >= DASH_RANGE_SQ) {
            boss.setState(boss.getPhase() == 1 ? AnubisEntity.States.DASH_P1 : AnubisEntity.States.DASH_P2);
            dashTargetPos = target.getPos();
        } else {
            moveTowardsTarget(target);
        }
    }

    private AnubisEntity.States selectMeleeAttack() {
        int phase = boss.getPhase();
        int rand = boss.getRandom().nextInt(3);
        return phase == 1 ?
                rand == 0 ? AnubisEntity.States.ATTACK_1_P1 :
                        rand == 1 ? AnubisEntity.States.ATTACK_2_P1 : AnubisEntity.States.ATTACK_3_P1 :
                rand == 0 ? AnubisEntity.States.ATTACK_4_P2 :
                        rand == 1 ? AnubisEntity.States.ATTACK_5_P2 : AnubisEntity.States.ATTACK_6_P2;
    }

    private void moveTowardsTarget(LivingEntity target) {
        Vec3d direction = target.getPos().subtract(boss.getPos()).normalize();
        boss.setVelocity(direction.multiply(0.2).add(0, boss.getVelocity().y, 0));
    }

    public Vec3d getDashTargetPos() {
        return dashTargetPos;
    }
}