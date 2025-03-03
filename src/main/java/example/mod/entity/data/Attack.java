package example.mod.entity.data;

import example.mod.entity.bosses.anubis.AnubisEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;

public interface Attack {
    int getDuration();
    float getDamage();
    void execute(ServerWorld world, AnubisEntity boss, LivingEntity target, DamageSource source, int tick);
    boolean shouldExecute(int tick);
}