package example.mod.utils;

public class ScreenShakeHandler {
    private static float shakeIntensity = 0.0f;
    private static int shakeTicks = 0;

    public static void addShake(float intensity, int ticks) {
        shakeIntensity = intensity;
        shakeTicks = ticks;
    }

    public static float getShakeIntensity() {
        return shakeIntensity;
    }

    public static int getShakeTicks() {
        return shakeTicks;
    }

    public static void tick() {
        if (shakeTicks > 0) {
            shakeTicks--;
            if (shakeTicks <= 0) {
                shakeIntensity = 0.0f;
            }
        }
    }
}