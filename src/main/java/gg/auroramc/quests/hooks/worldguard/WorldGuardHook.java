package gg.auroramc.quests.hooks.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.hooks.Hook;
import gg.auroramc.quests.util.FlagManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;

public class WorldGuardHook implements Hook {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new WorldGuardListener(), plugin);
        AuroraQuests.logger().info("Hooked into WorldGuard for flags, region filters and ENTER_REGION objective.");
    }

    @Override
    public void hookAtStartUp(AuroraQuests plugin) {
        FlagManager.registerFlags();
    }

    public boolean isBlocked(Player player) {
        return FlagManager.isBlocked(player);
    }

    public boolean isBlocked(Player player, Location location) {
        return FlagManager.isBlocked(player, location);
    }

    public boolean isInAnyRegion(Player player, Location location, Set<String> checkRegions) {
        World world = location.getWorld();
        if (world == null) return false;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

        RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
        if (regions == null) {
            return false;
        }

        ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint());

        var region = set.getRegions().stream().filter(r -> checkRegions.contains(r.getId())).findFirst().orElse(null);

        return region != null;
    }
}
