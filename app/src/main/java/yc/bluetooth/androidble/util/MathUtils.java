package yc.bluetooth.androidble.util;

public final class MathUtils {

    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    public static float project(float value, float min, float max, float newMin, float newMax) {
        return (value - min) * (newMax - newMin) / (max - min) + newMin;
    }
}
