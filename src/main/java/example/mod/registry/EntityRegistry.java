package example.mod.registry;

import example.mod.ExampleMod;
import example.mod.entity.bosses.anubis.AnubisEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class EntityRegistry {
    public static final EntityType<AnubisEntity> ANUBIS_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            ExampleMod.createId("anubis"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, AnubisEntity::new)
                    .dimensions(EntityDimensions.fixed(2f, 5f)).build()
    );
    public static void init() {
        FabricDefaultAttributeRegistry.register(ANUBIS_ENTITY, AnubisEntity.createAttributes());
        ExampleMod.LOGGER.info("Registering Entities for " + ExampleMod.MOD_ID);
    }
}
