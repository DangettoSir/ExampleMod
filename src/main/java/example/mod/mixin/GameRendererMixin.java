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
            float time = (ScreenShakeHandler.getMaxShakeTicks() - ScreenShakeHandler.getShakeTicks() + tickDelta) / 20.0f;
            float intensity = ScreenShakeHandler.getShakeIntensity() * (ScreenShakeHandler.getShakeTicks() / (float) ScreenShakeHandler.getMaxShakeTicks());
            float frequency = 2.0f;
            float shakeX = (float) Math.sin(time * frequency * 2 * Math.PI) * intensity * 0.1f;
            float shakeY = (float) Math.abs(Math.cos(time * frequency * 2 * Math.PI)) * intensity * 0.06f;
            matrices.translate(shakeX, shakeY, 0.0);
        }
    }
}