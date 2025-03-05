package example.mod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

public abstract class PhasedBossEntity extends BossEntity {
    private static final TrackedData<Integer> PHASE = DataTracker.registerData(PhasedBossEntity.class, TrackedDataHandlerRegistry.INTEGER);

    protected PhasedBossEntity(EntityType<? extends HostileEntity> entityType, World world, BossBar.Color barColor, String bossIdentifier) {
        super(entityType, world, barColor, bossIdentifier);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(PHASE, 1);
    }

    public void setPhase(int phase) {
        this.dataTracker.set(PHASE, phase);
        onPhaseChange(phase);
    }

    public int getPhase() {
        return this.dataTracker.get(PHASE);
    }

    protected abstract void onPhaseChange(int newPhase);
}