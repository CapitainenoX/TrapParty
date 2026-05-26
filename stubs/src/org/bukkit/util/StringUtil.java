package org.bukkit.util;
import java.util.Collection;
public final class StringUtil {
    public static <T extends Collection<? super String>> T copyPartialMatches(String token, Iterable<String> originals, T collection){return collection;}
}
