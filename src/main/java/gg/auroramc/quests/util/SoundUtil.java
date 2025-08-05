package gg.auroramc.quests.util;

import gg.auroramc.quests.AuroraQuests;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;

public class SoundUtil {
    public static void playSound(Player player, String sound, float volume, float pitch) {
        var key = NamespacedKey.fromString(sound);
        if (key != null) {
            var realSound = Registry.SOUNDS.get(key);
            if (realSound != null) {
                player.playSound(player.getLocation(), realSound, volume, pitch);
            }
        } else {
            AuroraQuests.logger().warning("Invalid sound key: " + sound);
        }
    }
}
