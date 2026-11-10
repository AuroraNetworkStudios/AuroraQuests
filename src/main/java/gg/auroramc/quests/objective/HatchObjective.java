package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HatchObjective extends TypedObjective {
    private static final TypeId SNIFFER = TypeId.fromString("minecraft:sniffer");
    private final boolean tracksSniffer;
    private final Set<Location> placedSnifferEggs = ConcurrentHashMap.newKeySet();

    public HatchObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
        tracksSniffer = definition.getArgs().getStringList("types").stream()
                .map(TypeId::fromString).anyMatch(SNIFFER::equals);
    }

    @Override
    protected void activate() {
        onEvent(PlayerEggThrowEvent.class, this::handle, EventPriority.MONITOR);
        // Only quests that list sniffer see it; empty types would otherwise start counting sniffers
        if (!tracksSniffer) return;
        onEvent(BlockPlaceEvent.class, this::onSnifferEggPlace, EventPriority.MONITOR);
        onEvent(BlockBreakEvent.class, this::onSnifferEggBreak, EventPriority.MONITOR);
        onEvent(CreatureSpawnEvent.class, this::onSnifferHatch, EventPriority.MONITOR);
    }

    public void handle(PlayerEggThrowEvent event) {
        if (!event.isHatching()) return;
        progress(event.getNumHatches(), meta(event.getEgg().getLocation(), event.getHatchingType()));
    }

    public void onSnifferEggPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.SNIFFER_EGG) return;

        var eggLocation = event.getBlock().getLocation();
        if (event.getPlayer() == data.profile().getPlayer()) {
            placedSnifferEggs.add(eggLocation);
        } else {
            placedSnifferEggs.remove(eggLocation);
        }
    }

    public void onSnifferEggBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.SNIFFER_EGG) return;
        placedSnifferEggs.remove(event.getBlock().getLocation());
    }

    public void onSnifferHatch(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.EGG) return;
        if (event.getEntityType() != EntityType.SNIFFER) return;

        var hatchLocation = event.getLocation();
        // Block location, not toBlockLocation(): the sniffer spawns with a random yaw and Location.equals compares it
        if (!placedSnifferEggs.remove(hatchLocation.getBlock().getLocation())) return;

        data.profile().getPlayer().getScheduler().run(AuroraQuests.getInstance(), (task) -> progress(1, meta(hatchLocation, SNIFFER)), null);
    }
}
