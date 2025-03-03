package example.mod.entity.data;

import net.minecraft.sound.SoundEvent;

public interface BossProperties {
    int getXp();
    SoundEvent getBossMusic();
    boolean hasBossMusic();
    int getTicksUntilDeath();
    default int getXpDefault() { return 0; }
    default SoundEvent getBossMusicDefault() { return null; }
    default boolean hasBossMusicDefault() { return false; }
    default boolean isFireImmuneDefault() { return false; }
    default int getTicksUntilDeathDefault() { return 20; }
}
