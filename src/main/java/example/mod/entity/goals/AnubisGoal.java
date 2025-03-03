package example.mod.entity.goals;

import example.mod.entity.bosses.anubis.AnubisEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

import static example.mod.utils.ParticleHelper.*;

public class AnubisGoal extends MeleeAttackGoal {

    private final AnubisEntity boss;
    private int attackCooldown;
    private int attackStatus;
    private Vec3d dashTargetPos;

    public AnubisGoal(AnubisEntity boss) {
        super(boss, 0.4D, true);
        this.boss = boss;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    private int getAttackDuration(AnubisEntity.States state) {
        return switch (state) {
            case DASH -> 85;      // 4.25 секунды
            case ATTACK_1 -> 80;  // 4 секунды
            case ATTACK_2 -> 60;  // 3 секунды
            case ATTACK_3 -> 60;  // 3 секунды
            case ATTACK_4 -> 112; // 5.58 секунды
            case ATTACK_5 -> 60;  // 3 секунды
            case ATTACK_6 -> 52;  // 2.6 секунды
            case ATTACK_7 -> 190; // 9.5 секунд
            default -> 20;        // IDLE
        };
    }

    public void reset() {
        this.attackStatus = 0;
        this.attackCooldown = 20;
        this.boss.setState(AnubisEntity.States.IDLE);
        this.dashTargetPos = null;
    }

    @Override
    public void start() {
        super.start();
        this.boss.setAttacking(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.boss.setAttacking(false);
        this.reset();
    }

    protected boolean isInMeleeRange(LivingEntity target) {
        double distance = this.boss.squaredDistanceTo(target);
        return distance <= 16.0; // 4 блока в квадрате
    }

    protected boolean isInDashRange(LivingEntity target) {
        double distance = this.boss.squaredDistanceTo(target);
        return distance >= 100.0;
    }

    private void randomAttackOrDash(LivingEntity target) {
        if (target == null) {
            this.boss.setState(AnubisEntity.States.IDLE);
            return;
        }

        int phase = this.boss.getPhase();
        if (this.isInMeleeRange(target)) {
            int rand = this.boss.getRandom().nextInt(phase == 1 ? 3 : 4);
            if (phase == 1) {
                switch (rand) {
                    case 0 -> this.boss.setState(AnubisEntity.States.ATTACK_1);
                    case 1 -> this.boss.setState(AnubisEntity.States.ATTACK_3);
                    case 2 -> this.boss.setState(AnubisEntity.States.ATTACK_6);
                }
            } else {
                switch (rand) {
                    case 0 -> this.boss.setState(AnubisEntity.States.ATTACK_1);
                    case 1 -> this.boss.setState(AnubisEntity.States.ATTACK_2);
                    case 2 -> this.boss.setState(AnubisEntity.States.ATTACK_4);
                    case 3 -> this.boss.setState(AnubisEntity.States.ATTACK_5);
                }
            }
        } else if (this.isInDashRange(target)) {
            this.boss.setState(AnubisEntity.States.DASH);
            this.dashTargetPos = target.getPos();
        } else {
            Vec3d direction = target.getPos().subtract(this.boss.getPos()).normalize();
            this.boss.setVelocity(direction.x * 0.6, this.boss.getVelocity().y, direction.z * 0.6);
            this.boss.setState(AnubisEntity.States.IDLE);
        }
    }

    @Override
    public void tick() {
        if (this.boss.isDead()) return;

        this.attackCooldown--;
        this.attackStatus++;

        LivingEntity target = this.boss.getTarget();
        ServerWorld world = (ServerWorld) this.boss.getWorld();

        if (target != null) {
            this.boss.setAttacking(true);
            this.boss.getLookControl().lookAt(target.getX(), target.getEyeY(), target.getZ());

            AnubisEntity.States currentState = this.boss.getState();

            if (currentState != AnubisEntity.States.IDLE && currentState != AnubisEntity.States.DEATH) {
                if (currentState == AnubisEntity.States.DASH) {
                    if (this.attackStatus == 20) { // Подготовка
                        this.boss.setVelocity(0, 0, 0);
                        spawnDirectionalParticles(world, ParticleTypes.SMOKE, this.boss.getPos(), 30, 2.0);
                        world.playSound(null, this.boss.getBlockPos(), SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 1.0f, 1.0f);
                    } else if (this.attackStatus >= 30 && this.attackStatus < 69) { // Полёт к игроку
                        double dx = target.getX() - this.boss.getX();
                        double dz = target.getZ() - this.boss.getZ();
                        double dy = target.getY() + 1.5 - this.boss.getY();
                        double distance = Math.sqrt(dx * dx + dz * dz);
                        if (distance > 0) {
                            double speed = distance / (69 - this.attackStatus);
                            double height = (this.attackStatus == 30) ? 3.0 : (dy / (69 - this.attackStatus));
                            this.boss.setVelocity(dx / distance * speed, height, dz / distance * speed);
                            spawnDirectionalParticles(world, ParticleTypes.FLAME, this.boss.getPos(), 10, 1.0);
                        }
                    } else if (this.attackStatus == 69) { // Приземление
                        this.boss.setPosition(target.getX(), target.getY(), target.getZ());
                        spawnRadiusParticles(world, this.boss.getPos(), 4.0);
                        triggerScreenshake(world, this.boss.getPos(), 1.0f);
                        world.playSound(null, this.boss.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0f, 1.0f);
                        this.attack(target, 0); // Урон от дэша
                    }
                } else {
                    this.boss.setVelocity(0, this.boss.getVelocity().y, 0);
                    this.boss.getNavigation().stop();
                    this.attack(target, this.boss.squaredDistanceTo(target));
                }

                if (this.attackStatus >= getAttackDuration(currentState)) {
                    this.reset();
                }
                return;
            }

            if (this.attackCooldown > 0) {
                this.boss.setState(AnubisEntity.States.IDLE);
            } else if (currentState == AnubisEntity.States.IDLE) {
                this.randomAttackOrDash(target);
                this.attackStatus = 0;
            }
            if (currentState == AnubisEntity.States.IDLE && !this.isInMeleeRange(target) && !this.isInDashRange(target)) {
                Vec3d direction = target.getPos().subtract(this.boss.getPos()).normalize();
                this.boss.setVelocity(direction.x * 0.6, this.boss.getVelocity().y, direction.z * 0.6);
            }
        } else {
            this.boss.setState(AnubisEntity.States.IDLE);
            this.reset();
        }
    }

    @Override
    protected void attack(LivingEntity target, double squaredDistance) {
        AnubisEntity.States currentState = this.boss.getState();
        ServerWorld world = (ServerWorld) this.boss.getWorld();
        DamageSource source = this.boss.getWorld().getDamageSources().mobAttack(this.boss);

        if (currentState != AnubisEntity.States.DASH && !this.isInMeleeRange(target)) return;

        switch (currentState) {
            case DASH -> {
                if (this.attackStatus == 69) {
                    for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class,
                            this.boss.getBoundingBox().expand(4.0), e -> e != this.boss)) {
                        entity.damage(source, 6.0f);
                    }
                }
            }
            case ATTACK_1 -> { // 4 секунды (80 тиков)
                if (this.attackStatus == 24 || this.attackStatus == 36) {
                    for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class,
                            this.boss.getBoundingBox().expand(4.0), e -> e != this.boss)) {
                        entity.damage(source, 8.0f);
                    }
                    spawnRadiusParticles(world, this.boss.getPos(), 4.0);
                    spawnDirectionalParticles(world, ParticleTypes.SMOKE, this.boss.getPos(), 15, 3.0);
                    triggerScreenshake(world, this.boss.getPos(), 0.5f);
                    world.playSound(null, this.boss.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0f, 1.0f);
                }
            }
            case ATTACK_2 -> { // 3 секунды (60 тиков)
                if (this.attackStatus == 36) {
                    for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class,
                            this.boss.getBoundingBox().expand(4.0), e -> e != this.boss)) {
                        entity.damage(source, 10.0f);
                    }
                    spawnRadiusParticles(world, this.boss.getPos(), 4.0);
                    spawnRadialWaveParticles(world, this.boss.getPos(), 5.0);
                    triggerScreenshake(world, this.boss.getPos(), 1.0f);
                    world.playSound(null, this.boss.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 1.0f, 1.0f);
                }
            }
            case ATTACK_3 -> { // 3 секунды (60 тиков)
                if (this.attackStatus == 30) {
                    target.damage(source, 6.0f);
                    spawnDirectionalParticles(world, ParticleTypes.CRIT, this.boss.getPos().add(0, 2.5, 0), 20, 4.0);
                    triggerScreenshake(world, this.boss.getPos(), 0.7f);
                    world.playSound(null, this.boss.getBlockPos(),
                            SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.HOSTILE, 1.0f, 1.0f);
                }
            }
            case ATTACK_4 -> { // 5.58 секунд (112 тиков)
                if (this.attackStatus == 31) {
                    target.damage(source, 9.0f);
                    spawnRadiusParticles(world, this.boss.getPos(), 4.0);
                    triggerScreenshake(world, this.boss.getPos(), 0.8f);
                    world.playSound(null, this.boss.getBlockPos(),
                            SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.0f, 1.0f);
                }
            }
            case ATTACK_5 -> { // 3 секунды (60 тиков)
                if (this.attackStatus == 28) {
                    target.damage(source, 7.0f);
                    spawnDirectionalParticles(world, ParticleTypes.SWEEP_ATTACK, this.boss.getPos().add(0, 2.0, 0), 15, 3.0);
                    triggerScreenshake(world, this.boss.getPos(), 0.6f);
                    world.playSound(null, this.boss.getBlockPos(),
                            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0f, 1.0f);
                }
            }
            case ATTACK_6 -> { // 2.6 секунды (52 тика)
                if (this.attackStatus == 40) {
                    for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class,
                            this.boss.getBoundingBox().expand(4.0), e -> e != this.boss)) {
                        entity.damage(source, 7.0f);
                    }
                    spawnRadiusParticles(world, this.boss.getPos(), 4.0);
                    triggerScreenshake(world, this.boss.getPos(), 0.8f);
                    world.playSound(null, this.boss.getBlockPos(),
                            SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 1.0f, 1.0f);
                }
            }
            case ATTACK_7 -> { // 9.5 секунд (190 тиков)
                if (this.attackStatus == 189) {
                    spawnRadiusParticles(world, this.boss.getPos(), 5.0);
                    triggerScreenshake(world, this.boss.getPos(), 1.5f);
                    world.playSound(null, this.boss.getBlockPos(), SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.HOSTILE, 1.0f, 1.0f);
                }
            }
            default -> {
            }
        }
    }
}