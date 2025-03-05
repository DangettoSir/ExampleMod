package example.mod;

import example.mod.client.entities.bosses.BossBarClient;
import example.mod.entity.bosses.anubis.AnubisRenderer;
import example.mod.entity.bosses.something.GeoBoneExplorerRenderer;
import example.mod.registry.EntityRegistry;
import example.mod.registry.NetworkingRegistry;
import example.mod.registry.client.BossBarClientRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NetworkingRegistry.registerS2CPackets();
        EntityRendererRegistry.register(EntityRegistry.ANUBIS_ENTITY, AnubisRenderer::new);
        EntityRendererRegistry.register(EntityRegistry.GEOBONE_EXPLORER_ENTITY, GeoBoneExplorerRenderer::new);
        BossBarClient.init();
        BossBarClientRegistry.init();
    }

}
