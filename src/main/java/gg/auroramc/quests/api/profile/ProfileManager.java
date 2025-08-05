package gg.auroramc.quests.api.profile;

import gg.auroramc.aurora.api.user.AuroraUser;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProfileManager {
    private final Map<UUID, Profile> profileMap = new ConcurrentHashMap<>();

    public void createProfile(AuroraUser user) {
        profileMap.put(user.getUniqueId(), new Profile(user));
    }

    public Profile getProfile(Player player) {
        return profileMap.get(player.getUniqueId());
    }

    public void destroyProfile(UUID uuid) {
        var profile = profileMap.remove(uuid);
        if (profile != null) {
            profile.destroy();
        }
    }

    public Collection<Profile> getProfiles() {
        return profileMap.values();
    }
}
