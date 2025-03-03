package example.mod.registry;

import example.mod.ExampleMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ItemRegistry {
    public static <E extends Item> E register(E item, String name) {
        Registry.register(Registries.ITEM, ExampleMod.createId(name), item);
        return item;
    }
    public static Item registerSimpleItem(String name) {
        return register(new Item(new FabricItemSettings()), name);
    }

    public static void init() {
        ExampleMod.LOGGER.info("Registering Mod Items for " + ExampleMod.MOD_ID);
    }
}
