package gg.auroramc.quests.util;

import gg.auroramc.aurora.api.dependency.Dep;
import gg.auroramc.aurora.api.dependency.DependencyManager;
import org.bukkit.event.EventPriority;

public class AnnoyingPluginUtil {
    public static EventPriority getBlockDropItemPriority() {
        if (DependencyManager.hasDep(Dep.ECO)) {
            return EventPriority.NORMAL;
        } else {
            return EventPriority.MONITOR;
        }
    }
}
