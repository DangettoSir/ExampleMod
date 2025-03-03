package example.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;

public class HitboxCommand {
    private static boolean isAdjusting = false;
    private static double offsetX = 0.0;
    private static double offsetY = 0.0;
    private static double offsetZ = 0.0;
    private static double scaleX = 1.0;
    private static double scaleY = 1.0;
    private static double scaleZ = 1.0;
    private static float rotX = 0.0f;
    private static float rotY = 0.0f;
    private static float rotZ = 0.0f;
    private static Entity targetEntity = null;
    private static String targetBone = null;
    private static BlockPos hitboxPos = null;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("createHitbox")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    PlayerEntity player = source.getPlayer();
                    if (player == null) return 0;

                    if (!isAdjusting) {
                        hitboxPos = player.getBlockPos();
                        isAdjusting = true;
                        player.sendMessage(Text.literal("Hitbox Adjustment Mode Enabled at " + hitboxPos + ". " +
                                "WASD (X/Z), QE (Y), RF (Scale X), TG (Scale Y), YH (Scale Z), IU (Rot X), JO (Rot Y), KL (Rot Z), P to log, /selectEntity, /selectBone <name>."), false);
                        lockPlayerMovement(player, true);
                    } else {
                        isAdjusting = false;
                        player.sendMessage(Text.literal("Hitbox Adjustment Mode Disabled."), false);
                        lockPlayerMovement(player, false);
                    }
                    return 1;
                })
        );

        dispatcher.register(CommandManager.literal("selectEntity")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    PlayerEntity player = source.getPlayer();
                    if (player == null || !isAdjusting) return 0;

                    World world = player.getEntityWorld();
                    List<Entity> entities = world.getEntitiesByClass(Entity.class, player.getBoundingBox().expand(10.0), entity -> true);
                    if (!entities.isEmpty()) {
                        Entity nearest = entities.stream()
                                .min(Comparator.comparingDouble(entity -> entity.squaredDistanceTo(player)))
                                .get();
                        targetEntity = nearest;
                        player.sendMessage(Text.literal("Selected entity: " + nearest.getName().getString()), false);
                    } else {
                        player.sendMessage(Text.literal("No entity found within 10 blocks."), false);
                    }
                    return 1;
                })
        );

        dispatcher.register(CommandManager.literal("selectBone")
                .then(CommandManager.argument("boneName", StringArgumentType.string())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            PlayerEntity player = source.getPlayer();
                            if (player == null || !isAdjusting || targetEntity == null) return 0;

                            String boneName = StringArgumentType.getString(context, "boneName");
                            targetBone = boneName;
                            player.sendMessage(Text.literal("Selected bone: " + boneName + " for entity " + targetEntity.getName().getString()), false);
                            return 1;
                        })
                )
        );
    }

    private static void lockPlayerMovement(PlayerEntity player, boolean lock) {
        if (lock) {
            player.setVelocity(0, 0, 0);
            player.setNoGravity(true);
            MinecraftClient.getInstance().options.forwardKey.setPressed(false);
            MinecraftClient.getInstance().options.backKey.setPressed(false);
            MinecraftClient.getInstance().options.leftKey.setPressed(false);
            MinecraftClient.getInstance().options.rightKey.setPressed(false);
            MinecraftClient.getInstance().options.jumpKey.setPressed(false);
            MinecraftClient.getInstance().options.sneakKey.setPressed(false);
        } else {
            player.setNoGravity(false);
        }
    }

    public static boolean isAdjusting() { return isAdjusting; }
    public static double getOffsetX() { return offsetX; }
    public static double getOffsetY() { return offsetY; }
    public static double getOffsetZ() { return offsetZ; }
    public static double getScaleX() { return scaleX; }
    public static double getScaleY() { return scaleY; }
    public static double getScaleZ() { return scaleZ; }
    public static float getRotX() { return rotX; }
    public static float getRotY() { return rotY; }
    public static float getRotZ() { return rotZ; }
    public static void setOffsetX(double value) { offsetX = value; }
    public static void setOffsetY(double value) { offsetY = value; }
    public static void setOffsetZ(double value) { offsetZ = value; }
    public static void setScaleX(double value) { scaleX = Math.max(0.1, value); }
    public static void setScaleY(double value) { scaleY = Math.max(0.1, value); }
    public static void setScaleZ(double value) { scaleZ = Math.max(0.1, value); }
    public static void setRotX(float value) { rotX = value; }
    public static void setRotY(float value) { rotY = value; }
    public static void setRotZ(float value) { rotZ = value; }
    public static BlockPos getHitboxPos() { return hitboxPos; }
    public static Entity getTargetEntity() { return targetEntity; }
    public static String getTargetBone() { return targetBone; }
}