package example.mod.entity.bosses.anubis;

import example.mod.ExampleMod;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class AnubisModel extends DefaultedEntityGeoModel<AnubisEntity> {
    public AnubisModel() {
        super(ExampleMod.createId("anubis"));
    }

    @Override
    public Identifier getAnimationResource(AnubisEntity entity) {
        return new Identifier(ExampleMod.MOD_ID, "animations/entities/anubis.animation.json");
    }

    @Override
    public Identifier getModelResource(AnubisEntity entity) {
        return new Identifier(ExampleMod.MOD_ID, "geo/entities/anubis.geo.json");
    }

    @Override
    public Identifier getTextureResource(AnubisEntity entity) {
        return new Identifier(ExampleMod.MOD_ID, "textures/entities/anubis.png");
    }
}