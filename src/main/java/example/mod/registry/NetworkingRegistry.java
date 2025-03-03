package example.mod.registry;

import example.mod.ExampleMod;
import example.mod.networking.PacketIds;
import example.mod.networking.S2C.ScreenShakeS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class NetworkingRegistry {
    public static void registerC2SPackets() {
        //ServerPlayNetworking.registerGlobalReceiver(PacketIds.{ID}, {NAME}C2S::receive);
    }

    public static void registerS2CPackets() {
        //ClientPlayNetworking.registerGlobalReceiver(PacketIds.{ID}, {NAME}S2C::receive);
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.SCREEN_SHAKE, ScreenShakeS2C::receive);
    }


    public static void init() {
        ExampleMod.LOGGER.info("Registering Networking for " + ExampleMod.MOD_ID);
    }

}
