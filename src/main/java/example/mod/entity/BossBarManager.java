package example.mod.entity;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BossBarManager {
    private final ServerBossBar bossBar;
    private final String bossIdentifier;

    public BossBarManager(Text displayName, BossBar.Color color, BossBar.Style style, String bossIdentifier) {
        this.bossBar = new ServerBossBar(displayName, color, style);
        this.bossBar.setDarkenSky(true);
        this.bossIdentifier = bossIdentifier;
    }

    public void updateHealth(float health, float maxHealth) {
        this.bossBar.setPercent(health / maxHealth);
    }

    public void setName(Text name) {
        this.bossBar.setName(name);
    }

    public void addPlayer(ServerPlayerEntity player) {
        this.bossBar.addPlayer(player);
    }

    public void removePlayer(ServerPlayerEntity player) {
        this.bossBar.removePlayer(player);
    }

    public String getBossIdentifier() {
        return bossIdentifier;
    }
}
