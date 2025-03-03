package example.mod.utils;

import example.mod.networking.PacketHelper;
import example.mod.networking.PacketIds;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleHelper {
    public static void spawnRadiusParticles(World world, Vec3d center, double radius) {
        if (world.isClient) return;
        int particleCount = 30;
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            double y = center.y + 1.0;
            ((ServerWorld) world).spawnParticles(ParticleTypes.SNOWFLAKE, x, y, z, 1, 0, 0, 0, 0);
        }
    }

    public static void spawnDirectionalParticles(ServerWorld world, ParticleEffect particle, Vec3d startPos, int count, double distance) {
        float yaw = world.getRandom().nextFloat() * 360.0F; // Используем случайный yaw, если нет сущности
        double yawRadians = Math.toRadians(yaw);
        double xDirection = -Math.sin(yawRadians);
        double zDirection = Math.cos(yawRadians);
        Vec3d direction = new Vec3d(xDirection, 0, zDirection).normalize(); // Горизонтальное направление

        for (int i = 0; i < count; i++) {
            double lerpFactor = (double) i / (count - 1);
            Vec3d currentPos = startPos.add(direction.multiply(distance * lerpFactor));
            world.spawnParticles(particle, currentPos.x, currentPos.y, currentPos.z, 1, 0, 0, 0, 0.0);
        }
    }

    public static void triggerScreenshake(ServerWorld world, Vec3d pos, float intensity) {
        world.getPlayers().forEach(player -> {
            if (player.squaredDistanceTo(pos.x, pos.y, pos.z) < 100) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeFloat(intensity);
                PacketHelper.sendToPlayerS2C(player, PacketIds.SCREEN_SHAKE, buf);
            }
        });
    }

    public static void spawnRadialWaveParticles(ServerWorld world, Vec3d center, double radius) {
        int particleCount = 50;
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            world.spawnParticles(ParticleTypes.EXPLOSION, x, center.y + 0.5, z, 1, 0, 0, 0, 0);
        }
    }

}
