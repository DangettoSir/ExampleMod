package example.mod.mixin;

import example.mod.client.entities.bosses.data.BossBarRenderer;
import example.mod.entity.bosses.anubis.AnubisEntity;
import example.mod.registry.client.BossBarClientRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, CallbackInfo ci) {
        BossBarHud hud = (BossBarHud) (Object) this;
        BossBarHudAccessor accessor = (BossBarHudAccessor) hud;
        Map<UUID, ClientBossBar> bossBars = accessor.getBossBars();

        boolean allCustom = true;
        for (ClientBossBar bossBar : bossBars.values()) {
            String bossName = bossBar.getName().getString().toLowerCase();
            boolean isCustom = false;
            Entity entity = null;

            assert MinecraftClient.getInstance().world != null;
            for (Entity e : MinecraftClient.getInstance().world.getEntities()) {
                if (e instanceof AnubisEntity anubis && anubis.getUuid().equals(bossBar.getUuid())) {
                    entity = anubis;
                    break;
                }
            }

            if (entity instanceof AnubisEntity anubis && !anubis.useCustomBossBar()) {
                allCustom = false;
                break;
            }

            for (BossBarRenderer renderer : BossBarClientRegistry.getRenderers()) {
                if (renderer.shouldRender(bossName)) {
                    isCustom = true;
                    break;
                }
            }
            if (!isCustom) {
                allCustom = false;
                break;
            }
        }
        if (allCustom) {
            ci.cancel();
        }
    }
}