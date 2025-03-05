package example.mod.entity.bosses.anubis;

import example.mod.entity.PhasedBossEntity;
import example.mod.entity.data.AnimationProvider;
import example.mod.entity.data.AnimationType;
import example.mod.entity.goals.anubis.AnubisGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AnubisEntity extends PhasedBossEntity implements GeoEntity {
    private static final TrackedData<Integer> STATE = DataTracker.registerData(AnubisEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> USE_CUSTOM_BOSS_BAR = DataTracker.registerData(AnubisEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean isDead = false;
    private boolean hasTransitioned = false;
    private int deathParticleTicks = 0;
    private boolean invulnerable = false;
    private int invulnerabilityTicks = 0;

    public AnubisEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world, BossBar.Color.YELLOW, "anubis");
        this.setNoGravity(false);
        this.lookControl = new LookControl(this);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(STATE, States.IDLE.ordinal());
        this.dataTracker.startTracking(USE_CUSTOM_BOSS_BAR, true);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new AnubisGoal(this));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(5, new RevengeGoal(this).setGroupRevenge());
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new MobNavigation(this, world);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        int currentPhase = getPhase();
        if (currentPhase == 1 && getHealth() <= getMaxHealth() / 2 && !hasTransitioned) {
            setPhase(2);
            hasTransitioned = true;
        }
        bossBarManager.setName(Text.literal("Anubis: Phase " + currentPhase));
    }

    @Override
    protected void onPhaseChange(int newPhase) {
        if (newPhase == 2) {
            setState(States.ATTACK_7_P2);
            setVelocity(0, getVelocity().y, 0);
            getWorld().playSound(null, getBlockPos(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.0f, 1.0f);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient) {
            LivingEntity target = getTarget();
            if (target != null && getState() == States.IDLE) {
                lookAtTarget(target);
                setBodyYaw(getYaw());
            }
            lookControl.tick();

            if (invulnerabilityTicks > 0) {
                invulnerabilityTicks--;
                if (invulnerabilityTicks <= 0) {
                    setInvulnerable(false);
                }
            }
        }
        if (isDead && !getWorld().isClient) {
            deathParticleTicks++;
            if (deathParticleTicks <= getTicksUntilDeath()) {
                ServerWorld world = (ServerWorld) getWorld();
                for (int i = 0; i < 20; i++) {
                    double offsetX = getRandom().nextGaussian() * 0.5;
                    double offsetY = getRandom().nextGaussian() * 0.5;
                    double offsetZ = getRandom().nextGaussian() * 0.5;
                    world.spawnParticles(ParticleTypes.SMOKE, getX() + offsetX, getY() + offsetY, getZ() + offsetZ, 1, 0, 0, 0, 0.1);
                }
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (isInvulnerable() || getState() == States.ATTACK_7_P2) return false;
        States state = getState();
        if (source.isIn(DamageTypeTags.IS_FALL) && (state == States.DASH_P1 || state == States.DASH_P2)) {
            return false;
        }
        return super.damage(source, amount);
    }

    public void setState(States state) {
        if (state.isValidForPhase(getPhase())) {
            this.dataTracker.set(STATE, state.ordinal());
        } else {
            throw new IllegalStateException("State " + state + " is not valid for phase " + getPhase());
        }
    }

    public States getState() {
        return States.values()[this.dataTracker.get(STATE)];
    }

    public void lookAtTarget(LivingEntity target) {
        if (target != null) {
            Vec3d direction = target.getPos().subtract(this.getPos()).normalize();
            float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
            float currentYaw = this.getYaw();
            float yawDelta = MathHelper.wrapDegrees(targetYaw - currentYaw);
            float newYaw = currentYaw + yawDelta;
            this.setYaw(newYaw);
            this.setHeadYaw(newYaw);
            this.setBodyYaw(newYaw);
            this.getLookControl().lookAt(target);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state ->
                state.setAndContinue(isDead ? AnimationType.DEAD.getAnimation(false) : getState().getAnimation(state.isMoving()))
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void setDeath() {
        isDead = true;
        setState(States.DEATH);
        getWorld().playSound(null, getBlockPos(), SoundEvents.ENTITY_WITHER_DEATH, SoundCategory.HOSTILE, 1.0f, 1.0f);
    }

    @Override
    public int getTicksUntilDeath() {
        return 71;
    }

    @Override
    public SoundEvent getBossMusic() {
        return null;
    }

    @Override
    public boolean hasBossMusic() {
        return false;
    }

    @Override
    public int getXp() {
        return 50;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
        if (invulnerable) {
            this.invulnerabilityTicks = 10;
        } else {
            this.invulnerabilityTicks = 0;
        }
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setUseCustomBossBar(boolean useCustom) {
        this.dataTracker.set(USE_CUSTOM_BOSS_BAR, useCustom);
    }

    public boolean useCustomBossBar() {
        return this.dataTracker.get(USE_CUSTOM_BOSS_BAR);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 5.0D);
    }

    public enum States implements AnimationProvider {
        IDLE(AnimationType.IDLE, StateCategory.BASIC, 0),
        DEATH(AnimationType.DEAD, StateCategory.BASIC, 0),
        DASH_P1(AnimationType.DASH, StateCategory.ATTACK, 1),
        ATTACK_1_P1(AnimationType.ATTACK_1, StateCategory.ATTACK, 1),
        ATTACK_2_P1(AnimationType.ATTACK_2, StateCategory.ATTACK, 1),
        ATTACK_3_P1(AnimationType.ATTACK_3, StateCategory.ATTACK, 1),
        DASH_P2(AnimationType.DASH, StateCategory.ATTACK, 2),
        ATTACK_4_P2(AnimationType.ATTACK_4, StateCategory.ATTACK, 2),
        ATTACK_5_P2(AnimationType.ATTACK_5, StateCategory.ATTACK, 2),
        ATTACK_6_P2(AnimationType.ATTACK_6, StateCategory.ATTACK, 2),
        ATTACK_7_P2(AnimationType.ATTACK_7, StateCategory.TRANSITION, 2);

        private final AnimationProvider animation;
        private final StateCategory category;
        private final int phase;

        States(AnimationProvider animation, StateCategory category, int phase) {
            this.animation = animation;
            this.category = category;
            this.phase = phase;
        }

        @Override
        public RawAnimation getAnimation(boolean isMoving) {
            return animation.getAnimation(isMoving);
        }

        public StateCategory getCategory() {
            return category;
        }

        public int getPhase() {
            return phase;
        }

        public boolean isValidForPhase(int currentPhase) {
            return phase == 0 || phase == currentPhase;
        }

        public enum StateCategory {
            BASIC,
            ATTACK,
            TRANSITION
        }
    }
}