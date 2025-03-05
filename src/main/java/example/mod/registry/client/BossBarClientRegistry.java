package example.mod.registry.client;

import example.mod.ExampleMod;
import example.mod.client.entities.bosses.DefaultBossBarRenderer;
import example.mod.client.entities.bosses.data.BossBarRenderer;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static example.mod.ExampleMod.createId;

public class BossBarClientRegistry {
    private static final List<BossBarRenderer> RENDERERS = new ArrayList<>();

    public static final Identifier ANUBIS_BOSS_BAR = createId("textures/gui/boss_bars/anubis_bar.png");
    public static final Identifier ANUBIS_BOSS_BAR_HP = createId("textures/gui/boss_bars/anubis_bar_hp.png");
    public static final Identifier ANUBIS_BOSS_BAR_HP_PHASE2 = createId("textures/gui/boss_bars/anubis_bar_hp_phase2.png");

    public static void init() {
        ExampleMod.LOGGER.info("Registering Boss Bar Ids and Renderers for " + ExampleMod.MOD_ID);
        registerBossBar("anubis", ANUBIS_BOSS_BAR, ANUBIS_BOSS_BAR_HP, ANUBIS_BOSS_BAR_HP_PHASE2);
    }

    public static void registerBossBar(String bossName, Identifier backgroundTexture, Identifier... hpTextures) {
        RENDERERS.add(new DefaultBossBarRenderer(bossName, backgroundTexture, hpTextures));
    }

    public static List<BossBarRenderer> getRenderers() {
        return RENDERERS;
    }
}
