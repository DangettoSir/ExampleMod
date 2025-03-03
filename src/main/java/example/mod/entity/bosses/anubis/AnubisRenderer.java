package example.mod.entity.bosses.anubis;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AnubisRenderer extends GeoEntityRenderer<AnubisEntity> {
    public AnubisRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new AnubisModel());
    }

    @Override
    public void render(AnubisEntity entity, float entityYaw, float partialTicks, MatrixStack stack, VertexConsumerProvider bufferIn, int packedLightIn) {
        float headYaw = MathHelper.lerp(partialTicks, entity.prevHeadYaw, entity.getHeadYaw());
        float headPitch = MathHelper.lerp(partialTicks, entity.prevPitch, entity.getHeadPitch());

        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
        GeoBone headBone = this.getGeoModel().getBone("bone12").orElse(null);
        if (headBone != null) {
            float yawRadians = (float) Math.toRadians(-headYaw);
            headBone.setRotY(yawRadians);

            float pitchRadians = (float) Math.toRadians(headPitch);
            headBone.setRotX(pitchRadians);
        }
    }

    @Override
    public RenderLayer getRenderType(AnubisEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick) {
        return RenderLayer.getEntityCutoutNoCull(texture);
    }
}