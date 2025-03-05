package example.mod.entity.bosses.something;

import example.mod.ExampleMod;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class GeoBoneExplorerModel extends DefaultedEntityGeoModel<GeoBoneExplorerEntity> {
    public GeoBoneExplorerModel() {
        super(ExampleMod.createId("anubis"), true);
    }

    @Override
    public Identifier getAnimationResource(GeoBoneExplorerEntity entity) {
        return new Identifier(ExampleMod.MOD_ID, "animations/entities/anubis.animation.json");
    }

    @Override
    public Identifier getModelResource(GeoBoneExplorerEntity entity) {
        return new Identifier(ExampleMod.MOD_ID, "geo/entities/anubis.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoBoneExplorerEntity entity) {
        return new Identifier(ExampleMod.MOD_ID, "textures/entities/anubis.png");
    }
}