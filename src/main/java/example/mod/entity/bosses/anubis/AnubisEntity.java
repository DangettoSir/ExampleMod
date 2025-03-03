package example.mod.entity.bosses.anubis;

import example.mod.entity.BossEntity;
import example.mod.entity.data.AnimationProvider;
import example.mod.entity.data.BossBarManager;
import example.mod.entity.goals.AnubisGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
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
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AnubisEntity extends BossEntity implements GeoEntity {
    private static final TrackedData<Integer> STATE = DataTracker.registerData(AnubisEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> HEAD_YAW = DataTracker.registerData(AnubisEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> HEAD_PITCH = DataTracker.registerData(AnubisEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean isDead = false;
    private int phase = 1;

    public AnubisEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world, BossBar.Color.PURPLE);
        this.bossBarManager = new BossBarManager(this.getDisplayName(), BossBar.Color.PURPLE, BossBar.Style.NOTCHED_10);
        this.setNoGravity(false);
        this.lookControl = new LookControl(this);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(STATE, States.IDLE.ordinal());
        this.dataTracker.startTracking(HEAD_YAW, 0.0F);
        this.dataTracker.startTracking(HEAD_PITCH, 0.0F);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 5.0D);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new AnubisGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        targetSelector.add(5, new RevengeGoal(this).setGroupRevenge());
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new MobNavigation(this, world);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (getHealth() <= getMaxHealth() / 2 && phase == 1) {
            phase = 2;
            setState(States.ATTACK_7);
            setVelocity(0, getVelocity().y, 0);
            getWorld().playSound(null, getBlockPos(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.0f, 1.0f);
        }

        LivingEntity target = getTarget();
        if (target != null && !getWorld().isClient) {
            lookControl.lookAt(target);
            dataTracker.set(HEAD_YAW, headYaw);
            dataTracker.set(HEAD_PITCH, getPitch());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient) lookControl.tick();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        States state = getState();
        if ((source.isIn(DamageTypeTags.IS_FALL) && state == States.DASH) || state == States.ATTACK_7) {
            return false;
        }
        return super.damage(source, amount);
    }

    public void setState(States state) {
        dataTracker.set(STATE, state.ordinal());
    }

    public States getState() {
        return States.values()[dataTracker.get(STATE)];
    }

    public int getPhase() {
        return phase;
    }

    public float getHeadYaw() {
        return dataTracker.get(HEAD_YAW);
    }

    public float getHeadPitch() {
        return dataTracker.get(HEAD_PITCH);
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

    @Override public int getXp() { return 50; }
    @Override public boolean isUndead() { return true; }
    @Override public int getTicksUntilDeath() { return 72; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_WITHER_SKELETON_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_WITHER_SKELETON_DEATH; }

    public enum States implements AnimationProvider {
        IDLE(AnimationType.IDLE), DASH(AnimationType.DASH),
        ATTACK_1(AnimationType.ATTACK_1), ATTACK_2(AnimationType.ATTACK_2),
        ATTACK_3(AnimationType.ATTACK_3), ATTACK_4(AnimationType.ATTACK_4),
        ATTACK_5(AnimationType.ATTACK_5), ATTACK_6(AnimationType.ATTACK_6),
        ATTACK_7(AnimationType.ATTACK_7), DEATH(AnimationType.DEAD);

        private final AnimationProvider animation;

        States(AnimationProvider animation) {
            this.animation = animation;
        }

        @Override
        public RawAnimation getAnimation(boolean isMoving) {
            return animation.getAnimation(isMoving);
        }
    }


}