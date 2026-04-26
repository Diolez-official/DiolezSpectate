package me.diolezz.diolezspectate.util;

import org.bukkit.ChatColor;

/**
 * Centralised message formatting utility.
 * All plugin messages go through here — easy to rebrand later.
 */
public final class Messages {

    private Messages() {}

    private static final String PREFIX_RAW = "&8[&6Diolezz&eSpectate&8] &r";

    public static String prefix() {
        return color(PREFIX_RAW);
    }

    /**
     * Translates §-style colour codes using & as the alternate colour char.
     */
    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}