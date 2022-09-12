package de.erdbeerbaerlp.lilyauth;

import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UUIDUtils {
    public static UUID getUUIDFromName(final String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    public static UUID getUUIDFromPlayer(final Player p) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + p.getName()).getBytes(StandardCharsets.UTF_8));
    }
}
