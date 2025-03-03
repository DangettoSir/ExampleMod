package example.mod.entity;

import example.mod.entity.data.BossBarManager;
import example.mod.entity.data.BossProperties;
import example.mod.utils.IAnimatedDeath;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class BossEntity extends HostileEntity implements IAnimatedDeath, BossProperties {

    protected BossBarManager bossBarManager;
    private boolean hasUpdatedHealth = false;
    private boolean playingMusic = false;
    protected int deathTime;

    protected BossEntity(EntityType<? extends HostileEntity> entityType, World world, BossBar.Color barColor) {
        super(entityType, world);
        this.bossBarManager = new BossBarManager(this.getDisplayName(), barColor, BossBar.Style.NOTCHED_10);
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
        if (hasBossMusic() && !this.getWorld().isClient && !playingMusic) {
            this.getWorld().playSound(null, this.getBlockPos(), getBossMusic(), SoundCategory.MUSIC, 1f, 1f);
            this.playingMusic = true;
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
        this.setDeath();
        if (this.getBossMusic() != null && this.getWorld() instanceof ServerWorld) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeIdentifier(this.getBossMusic().getId());
        }
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
        if (this.deathTime >= this.getTicksUntilDeath()) {
            this.remove(RemovalReason.KILLED);
        }
    }



    @Override
    public int getXp() { return getXpDefault(); }
    @Override
    public SoundEvent getBossMusic() { return getBossMusicDefault(); }
    @Override
    public boolean hasBossMusic() { return hasBossMusicDefault(); }
    @Override
    public boolean isFireImmune() { return isFireImmuneDefault(); }
    @Override
    public int getTicksUntilDeath() { return getTicksUntilDeathDefault(); }

    @Override
    public abstract void setDeath();

}