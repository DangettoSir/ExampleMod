package example.mod.entity.data;

import example.mod.entity.BossEntity;
import net.minecraft.world.World;

public interface BossEntityFactory<T extends BossEntity> {
    T create(World world);
}