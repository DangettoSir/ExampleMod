package example.mod;

import example.mod.command.HitboxCommand;
import example.mod.registry.EntityRegistry;
import example.mod.registry.ItemRegistry;
import example.mod.registry.NetworkingRegistry;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "examplemod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		EntityRegistry.init();
		ItemRegistry.init();
		NetworkingRegistry.init();
		NetworkingRegistry.registerC2SPackets();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			HitboxCommand.register(dispatcher, registryAccess);
		});
		LOGGER.info("Hello Fabric world!");
	}
	public static Identifier createId(String path) {
		return new Identifier(ExampleMod.MOD_ID, path);
	}
}