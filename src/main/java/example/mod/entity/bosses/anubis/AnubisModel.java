package example.mod.entity.bosses.anubis;

import example.mod.ExampleMod;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class AnubisModel extends DefaultedEntityGeoModel<AnubisEntity> {
    public AnubisModel() {
        super(ExampleMod.createId("anubis"), true);
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

    @Override
    public void setCustomAnimations(AnubisEntity animatable, long instanceId, AnimationState<AnubisEntity> animationState) {
        if (!this.turnsHead)
            return;

        CoreGeoBone head = getAnimationProcessor().getBone("bone12");

        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * MathHelper.RADIANS_PER_DEGREE);
            head.setRotY(entityData.netHeadYaw() * MathHelper.RADIANS_PER_DEGREE);
        }
    }
}