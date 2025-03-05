package example.mod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class BossEntity extends HostileEntity {
    protected BossBarManager bossBarManager;
    protected int deathTime;
    private boolean hasUpdatedHealth = false;

    protected BossEntity(EntityType<? extends HostileEntity> entityType, World world, BossBar.Color barColor, String bossIdentifier) {
        super(entityType, world);
        this.bossBarManager = new BossBarManager(this.getDisplayName(), barColor, BossBar.Style.NOTCHED_10, bossIdentifier);
        this.experiencePoints = getXp();
    }

    @Override
    protected void mobTick() {
        if (!this.hasUpdatedHealth) {
            this.setHealth(this.getMaxHealth());
            this.hasUpdatedHealth = true;
        }
        this.bossBarManager.updateHealth(this.getHealth(), this.getMaxHealth());
    }

    @Override
    public void tick() {
        super.tick();
        if (hasBossMusic() && !this.getWorld().isClient && !this.getDataTracker().get(new TrackedData<>(0, TrackedDataHandlerRegistry.BOOLEAN))) {
            this.getWorld().playSound(null, this.getBlockPos(), getBossMusic(), SoundCategory.MUSIC, 1f, 1f);
            this.getDataTracker().set(new TrackedData<>(0, TrackedDataHandlerRegistry.BOOLEAN), true);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("HasUpdatedHealth", this.hasUpdatedHealth);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (this.hasCustomName()) {
            this.bossBarManager.setName(this.getDisplayName());
        }
        this.hasUpdatedHealth = nbt.getBoolean("HasUpdatedHealth");
    }

    @Override
    public void setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        this.bossBarManager.setName(this.getDisplayName());
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBarManager.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBarManager.removePlayer(player);
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        setDeath();
    }

    @Override
    protected boolean shouldAlwaysDropXp() {
        return true;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean canStartRiding(Entity entity) {
        return false;
    }

    @Override
    public void updatePostDeath() {
        this.deathTime++;
        if (this.deathTime >= getTicksUntilDeath()) {
            this.remove(RemovalReason.KILLED);
        }
    }

    // Добавляем публичный метод для доступа к goalSelector
    public GoalSelector getGoalSelector() {
        return this.goalSelector;
    }

    public abstract void setDeath();
    public abstract int getTicksUntilDeath();
    public abstract SoundEvent getBossMusic();
    public abstract boolean hasBossMusic();
    public abstract int getXp();
}
