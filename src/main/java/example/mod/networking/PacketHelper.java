package example.mod.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PacketHelper {
    public static void sendToPlayerS2C(ServerPlayerEntity player, Identifier packetId, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, packetId, buf);
    }
}