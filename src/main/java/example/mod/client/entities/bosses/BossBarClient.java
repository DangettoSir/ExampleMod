package example.mod.client.entities.bosses;

import example.mod.client.entities.bosses.data.BossBarRenderer;
import example.mod.mixin.BossBarHudAccessor;
import example.mod.registry.client.BossBarClientRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;

import java.util.Map;
import java.util.UUID;

public class BossBarClient {
    public static void init() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.inGameHud == null) return;

            BossBarHud hud = client.inGameHud.getBossBarHud();
            BossBarHudAccessor accessor = (BossBarHudAccessor) hud;
            Map<UUID, ClientBossBar> bossBars = accessor.getBossBars();
            int yOffset = 20;

            for (ClientBossBar bossBar : bossBars.values()) {
                String bossName = bossBar.getName().getString().toLowerCase();
                for (BossBarRenderer renderer : BossBarClientRegistry.getRenderers()) {
                    if (renderer.shouldRender(bossName)) {
                        int phase = DefaultBossBarRenderer.getPhaseFromBossBar(client, bossBar);
                        renderer.render(client, context, bossBar, bossBar.getName(), context.getScaledWindowWidth() / 2 - 91, yOffset, phase);
                        yOffset += 42;
                        break;
                    }
                }
            }
        });
    }
}