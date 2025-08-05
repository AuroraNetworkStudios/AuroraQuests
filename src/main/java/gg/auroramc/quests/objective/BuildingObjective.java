package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.events.region.RegionBlockBreakEvent;
import gg.auroramc.aurora.expansions.region.RegionExpansion;
import gg.auroramc.quests.api.event.objective.PlayerBreakCustomBlockEvent;
import gg.auroramc.quests.api.event.objective.PlayerPlaceCustomBlockEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

public class BuildingObjective extends TypedObjective {

    public BuildingObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(BlockPlaceEvent.class, this::onBlockPlace, EventPriority.MONITOR);
        onEvent(PlayerPlaceCustomBlockEvent.class, this::onBlockPlace, EventPriority.MONITOR);
        onEvent(RegionBlockBreakEvent.class, this::onBlockBreak, EventPriority.MONITOR);
        onEvent(PlayerBreakCustomBlockEvent.class, this::onBlockBreak, EventPriority.MONITOR);
    }


    public void onBlockPlace(BlockPlaceEvent event) {
        var player = event.getPlayer();
        if (player != data.profile().getPlayer()) return;

        progress(1, meta(event.getBlock().getLocation(), event.getBlock().getType()));
    }

    public void onBlockPlace(PlayerPlaceCustomBlockEvent event) {
        progress(1, meta(event.getBlock().getLocation(), event.getType()));
    }

    public void onBlockBreak(RegionBlockBreakEvent event) {
        Player player = event.getPlayerWhoBroke();
        if (player != data.profile().getPlayer()) return;
        if (event.isNatural()) return;

        progress(-1, meta(event.getBlock().getLocation(), event.getBlock().getType()));
    }

    public void onBlockBreak(PlayerBreakCustomBlockEvent event) {
        var regionManager = AuroraAPI.getExpansions().getExpansion(RegionExpansion.class);
        if (regionManager == null) return;

        if (regionManager.isPlacedBlock(event.getBlock())) return;

        progress(-1, meta(event.getBlock().getLocation(), event.getType()));
    }

}
