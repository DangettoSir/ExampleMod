package example.mod.utils;

public class ScreenShakeHandler {
    private static float shakeIntensity = 0.0f;
    private static int shakeTicks = 0;
    private static int maxShakeTicks = 0;

    public static void addShake(float intensity, int ticks) {
        shakeIntensity = Math.max(shakeIntensity, intensity);
        shakeTicks = Math.max(shakeTicks, ticks);
        maxShakeTicks = shakeTicks;
    }

    public static float getShakeIntensity() {
        return shakeIntensity;
    }

    public static int getShakeTicks() {
        return shakeTicks;
    }

    public static int getMaxShakeTicks() {
        return maxShakeTicks;
    }

    public static void tick() {
        if (shakeTicks > 0) {
            shakeTicks--;
            if (shakeTicks <= 0) {
                shakeIntensity = 0.0f;
                maxShakeTicks = 0;
            }
        }
    }
}