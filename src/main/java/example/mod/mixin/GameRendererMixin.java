package example.mod.mixin;

import example.mod.utils.ScreenShakeHandler;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void applyScreenShake(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        ScreenShakeHandler.tick();
        if (ScreenShakeHandler.getShakeTicks() > 0) {
            float shake = ScreenShakeHandler.getShakeIntensity() *
                    (ScreenShakeHandler.getShakeTicks() / (float) Math.max(1, ScreenShakeHandler.getShakeTicks()));
            matrices.translate(
                    (Math.random() - 0.5) * shake,
                    (Math.random() - 0.5) * shake,
                    0.0
            );
        }
    }
}