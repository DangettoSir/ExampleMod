package example.mod.entity.bosses.something;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GeoBoneExplorerRenderer extends GeoEntityRenderer<GeoBoneExplorerEntity> {
    public GeoBoneExplorerRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new GeoBoneExplorerModel());
    }

    @Override
    public void render(GeoBoneExplorerEntity entity, float entityYaw, float partialTicks, MatrixStack stack,
                       VertexConsumerProvider bufferIn, int packedLightIn) {
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);

        stack.push();
        stack.pop();
    }



    @Override
    public Identifier getTextureLocation(GeoBoneExplorerEntity instance) {
        return new Identifier("examplemod", "textures/entities/anubis.png");
    }
}