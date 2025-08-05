package gg.auroramc.quests.api;

import gg.auroramc.quests.api.profile.ProfileManager;
import gg.auroramc.quests.api.questpool.PoolManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public abstract class AuroraQuestsPlugin extends JavaPlugin {
    protected static AuroraQuestsPlugin instance;

    public static AuroraQuestsPlugin inst() {
        return instance;
    }

    protected PoolManager poolManager;
    protected ProfileManager profileManager;
}
