package example.mod.entity.goals;

import example.mod.entity.bosses.anubis.AnubisEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import static example.mod.utils.ParticleHelper.spawnDirectionalParticles;
import static example.mod.utils.ParticleHelper.triggerScreenshake;

public class AttackEffect {
    private final ParticleEffect particle;
    private final int particleCount;
    private final double particleSpeed;
    private final SoundEvent sound;
    private final float shakeIntensity;
    private final double yOffset;

    AttackEffect(ParticleEffect particle, int particleCount, double particleSpeed, SoundEvent sound, float shakeIntensity, double yOffset) {
        this.particle = particle;
        this.particleCount = particleCount;
        this.particleSpeed = particleSpeed;
        this.sound = sound;
        this.shakeIntensity = shakeIntensity;
        this.yOffset = yOffset;
    }

    AttackEffect(ParticleEffect particle, int particleCount, double particleSpeed, SoundEvent sound, float shakeIntensity) {
        this(particle, particleCount, particleSpeed, sound, shakeIntensity, 0.0);
    }

    void apply(ServerWorld world, AnubisEntity boss) {
        if (particle != null) {
            spawnDirectionalParticles(world, particle, boss.getPos().add(0, yOffset, 0), particleCount, particleSpeed);
        }
        if (shakeIntensity > 0) {
            triggerScreenshake(world, boss.getPos(), shakeIntensity);
        }
        if (sound != null) {
            world.playSound(null, boss.getBlockPos(), sound, SoundCategory.HOSTILE, 1.0f, 1.0f);
        }
    }
}
