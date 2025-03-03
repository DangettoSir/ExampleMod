package example.mod.networking.S2C;

import example.mod.utils.ScreenShakeHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class ScreenShakeS2C {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        float intensity = buf.readFloat();
        client.execute(() -> {
            if (client.world != null && client.gameRenderer != null) {
                ScreenShakeHandler.addShake(intensity, 10);
            }
        });
    }
}