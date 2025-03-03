package example.mod.entity.bosses.anubis;

import example.mod.entity.BossEntity;
import example.mod.entity.data.BossBarManager;
import example.mod.entity.goals.AnubisGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.*;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;


public class AnubisEntity extends BossEntity implements GeoEntity {

    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation DASH_ANIM = RawAnimation.begin().thenPlay("dash");
    protected static final RawAnimation ATTACK_ANIM_1 = RawAnimation.begin().thenPlay("attack_1");
    protected static final RawAnimation ATTACK_ANIM_2 = RawAnimation.begin().thenPlay("attack_2");
    protected static final RawAnimation ATTACK_ANIM_3 = RawAnimation.begin().thenPlay("attack_3");
    protected static final RawAnimation ATTACK_ANIM_4 = RawAnimation.begin().thenPlay("attack_4");
    protected static final RawAnimation ATTACK_ANIM_5 = RawAnimation.begin().thenPlay("attack_5");
    protected static final RawAnimation ATTACK_ANIM_6 = RawAnimation.begin().thenPlay("attack_6");
    protected static final RawAnimation ATTACK_ANIM_7 = RawAnimation.begin().thenPlay("attack_7");
    protected static final RawAnimation DEAD_ANIM = RawAnimation.begin().thenPlay("dead");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean isDead = false;
    private int phase = 1;

    private static final TrackedData<Integer> STATE = DataTracker.registerData(AnubisEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> HEAD_YAW = DataTracker.registerData(AnubisEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> HEAD_PITCH = DataTracker.registerData(AnubisEntity.class, TrackedDataHandlerRegistry.FLOAT);

    public AnubisEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world, BossBar.Color.PURPLE);
        this.bossBarManager = new BossBarManager(this.getDisplayName(), BossBar.Color.PURPLE, BossBar.Style.NOTCHED_10);
        this.setNoGravity(false);
        this.lookControl = new LookControl(this);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(STATE, 0);
        this.dataTracker.startTracking(HEAD_YAW, 0.0F);
        this.dataTracker.startTracking(HEAD_PITCH, 0.0F);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 5.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new AnubisGoal(this));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
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
        if (this.getHealth() <= this.getMaxHealth() / 2 && this.phase == 1) {
            this.phase = 2;
            this.setState(States.ATTACK_7);
            this.setVelocity(0, this.getVelocity().y, 0);
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.0f, 1.0f);
        }

        LivingEntity target = this.getTarget();
        if (target != null && !this.getWorld().isClient) {
            double dx = target.getX() - this.getX();
            double dy = target.getEyeY() - (this.getY() + 1.5);
            double dz = target.getZ() - this.getZ();
            float yaw = (float) MathHelper.atan2(dz, dx) * (180F / (float)Math.PI) - 90F;
            float pitch = (float) -MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180F / (float)Math.PI);
            this.dataTracker.set(HEAD_YAW, yaw);
            this.dataTracker.set(HEAD_PITCH, pitch);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.IS_FALL) && this.getState() == States.DASH) {
            return false;
        }
        if (this.getState() == States.ATTACK_7) {
            return false;
        }
        return super.damage(source, amount);
    }

    public void setState(States state) {
        this.dataTracker.set(STATE, state.ordinal());
    }

    public States getState() {
        return States.values()[this.dataTracker.get(STATE)];
    }

    public int getPhase() {
        return this.phase;
    }

    public float getHeadYaw() {
        return this.dataTracker.get(HEAD_YAW);
    }

    public float getHeadPitch() {
        return this.dataTracker.get(HEAD_PITCH);
    }

    public enum States {
        IDLE, DASH, ATTACK_1, ATTACK_2, ATTACK_3, ATTACK_4, ATTACK_5, ATTACK_6, ATTACK_7, DEATH
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (this.isDead) {
                return state.setAndContinue(DEAD_ANIM);
            }
            return switch (this.getState()) {
                case IDLE -> state.setAndContinue(state.isMoving() ? WALK_ANIM : IDLE_ANIM);
                case DASH -> state.setAndContinue(DASH_ANIM);
                case ATTACK_1 -> state.setAndContinue(ATTACK_ANIM_1);
                case ATTACK_2 -> state.setAndContinue(ATTACK_ANIM_2);
                case ATTACK_3 -> state.setAndContinue(ATTACK_ANIM_3);
                case ATTACK_4 -> state.setAndContinue(ATTACK_ANIM_4);
                case ATTACK_5 -> state.setAndContinue(ATTACK_ANIM_5);
                case ATTACK_6 -> state.setAndContinue(ATTACK_ANIM_6);
                case ATTACK_7 -> state.setAndContinue(ATTACK_ANIM_7);
                default -> state.setAndContinue(IDLE_ANIM);
            };
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void setDeath() {
        this.isDead = true;
        this.setState(States.DEATH);
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WITHER_DEATH, SoundCategory.HOSTILE, 1.0f, 1.0f);
    }

    @Override
    public int getXp() {
        return 50;
    }

    @Override
    public boolean isUndead() {
        return true;
    }


    @Override
    public int getTicksUntilDeath() {
        return 72;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WITHER_SKELETON_DEATH;
    }

}