package example.mod;

import example.mod.entity.bosses.anubis.AnubisRenderer;
import example.mod.registry.EntityRegistry;
import example.mod.registry.NetworkingRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NetworkingRegistry.registerS2CPackets();
        EntityRendererRegistry.register(EntityRegistry.ANUBIS_ENTITY, AnubisRenderer::new);
    }

}
