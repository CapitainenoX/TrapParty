package fr.trapparty.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtil {
    private RandomUtil() {}

    public static <T> T pick(List<T> items) {
        if (items == null || items.isEmpty()) return null;
        return items.get(ThreadLocalRandom.current().nextInt(items.size()));
    }

    public static boolean chance(double p) {
        return ThreadLocalRandom.current().nextDouble() < p;
    }

    public static int between(int min, int max) {
        if (max <= min) return min;
        return min + ThreadLocalRandom.current().nextInt(max - min + 1);
    }
}
