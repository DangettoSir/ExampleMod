package example.mod.entity.bosses.anubis;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AnubisRenderer extends GeoEntityRenderer<AnubisEntity> {
    public AnubisRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new AnubisModel());
    }

    @Override
    public void render(AnubisEntity entity, float entityYaw, float partialTicks, MatrixStack stack, VertexConsumerProvider bufferIn, int packedLightIn) {
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);

        GeoBone headBone = this.getGeoModel().getBone("bone12").orElse(null);

        if (headBone != null) {
            float headYaw = entity.getHeadYaw();
            float headPitch = entity.getHeadPitch();
            headBone.setRotY((float) Math.toRadians(-headYaw));
            headBone.setRotX((float) Math.toRadians(headPitch));
        }
    }

    @Override
    public RenderLayer getRenderType(AnubisEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick) {
        return RenderLayer.getEntityCutoutNoCull(texture);
    }
}