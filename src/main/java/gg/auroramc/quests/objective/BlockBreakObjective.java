package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.events.region.RegionBlockBreakEvent;
import gg.auroramc.quests.api.event.objective.PlayerBreakCustomBlockEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class BlockBreakObjective extends TypedObjective {

    public BlockBreakObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(RegionBlockBreakEvent.class, this::onBlockBreak, EventPriority.MONITOR);
        onEvent(PlayerBreakCustomBlockEvent.class, this::onBlockBreak, EventPriority.MONITOR);
    }

    public void onBlockBreak(RegionBlockBreakEvent e) {
        if (!e.isNatural()) return;
        if (e.getPlayerWhoBroke() != data.profile().getPlayer()) return;

        progress(1, meta(e.getBlock().getLocation(), e.getBlock().getType()));
    }

    public void onBlockBreak(PlayerBreakCustomBlockEvent event) {
        if (AuroraAPI.getRegionManager().isPlacedBlock(event.getBlock())) return;
        progress(1, meta(event.getBlock().getLocation(), event.getType()));
    }
}
