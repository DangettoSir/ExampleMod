package example.mod.client.entities.bosses.data;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;

public interface BossBarRenderer {
    void render(MinecraftClient client, DrawContext context, ClientBossBar bossBar, Text name, int x, int y, int phase);
    boolean shouldRender(String bossName);
}