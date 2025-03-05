package example.mod.client.entities.bosses;

import example.mod.client.entities.bosses.data.BossBarRenderer;
import example.mod.entity.bosses.anubis.AnubisEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DefaultBossBarRenderer implements BossBarRenderer {
    private final String bossName;
    private final Identifier backgroundTexture;
    private final Identifier[] hpTextures;

    public DefaultBossBarRenderer(String bossName, Identifier backgroundTexture, Identifier... hpTextures) {
        this.bossName = bossName.toLowerCase();
        this.backgroundTexture = backgroundTexture;
        this.hpTextures = hpTextures.length > 0 ? hpTextures : new Identifier[]{backgroundTexture};
    }

    @Override
    public void render(MinecraftClient client, DrawContext context, ClientBossBar bossBar, Text name, int x, int y, int phase) {
        int textureX = x + (182 - 159) / 2;

        // Фон
        context.drawTexture(backgroundTexture, textureX, y, 48, 18, 159, 27, 256, 64);

        // Текстуры HP
        int healthBarWidth = (int) (bossBar.getPercent() * 143);
        if (healthBarWidth > 0) {
            for (int i = 0; i <= Math.min(phase - 1, hpTextures.length - 1); i++) {
                context.drawTexture(hpTextures[i], textureX + 8, y + 12, 56, 30, healthBarWidth, 4, 256, 64);
            }
        }

        // Имя
        float textX = textureX + 159 / 2f - client.textRenderer.getWidth(name) / 2f;
        context.drawTextWithShadow(client.textRenderer, name, (int) textX, y - 10, 0xFFFFFF);
    }

    @Override
    public boolean shouldRender(String bossName) {
        return bossName.toLowerCase().contains(this.bossName);
    }

    public static int getPhaseFromBossBar(MinecraftClient client, ClientBossBar bossBar) {
        if (client.world != null) {
            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof AnubisEntity anubis && anubis.getUuid().equals(bossBar.getUuid())) {
                    return anubis.getPhase();
                }
            }
        }
        return 1;
    }
}