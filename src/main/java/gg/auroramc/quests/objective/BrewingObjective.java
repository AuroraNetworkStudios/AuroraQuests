package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.util.BukkitPotionType;
import gg.auroramc.aurora.api.util.Version;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.ObjectiveMeta;
import gg.auroramc.quests.api.objective.StringTypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class BrewingObjective extends StringTypedObjective {
    private final Map<Location, Player> brewingStands = new ConcurrentHashMap<>();

    public BrewingObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(InventoryOpenEvent.class, this::onInventoryOpen, EventPriority.MONITOR);
        onEvent(BrewEvent.class, this::onBrew, EventPriority.MONITOR);
    }


    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        final Inventory inventory = event.getInventory();
        if (!(inventory instanceof BrewerInventory)) {
            return;
        }

        final InventoryHolder holder = inventory.getHolder();
        if (holder == null) {
            return;
        }

        if (player == data.profile().getPlayer()) {
            brewingStands.put(inventory.getLocation(), player);
        } else {
            brewingStands.remove(inventory.getLocation());
        }
    }

    public void onBrew(BrewEvent event) {
        var player = brewingStands.remove(event.getBlock().getLocation());
        if (player == null) return;

        final var currentResults = Stream.of(event.getContents().getStorageContents())
                .map(stack -> stack == null ? ItemStack.empty() : stack.clone())
                .toArray(ItemStack[]::new);

        Bukkit.getRegionScheduler().run(AuroraQuests.getInstance(), event.getBlock().getLocation(), (t) -> {
            final var newResults = Stream.of(event.getContents().getStorageContents())
                    .map(stack -> stack == null ? ItemStack.empty() : stack)
                    .toArray(ItemStack[]::new);

            var metas = new ArrayList<ObjectiveMeta>();

            for (int i = 0; i < 3; i++) {
                if (currentResults[i].equals(newResults[i])) {
                    // Didn't change, meaning it was already there
                    continue;
                }
                var item = newResults[i];

                if (!item.isEmpty() && item.hasItemMeta() && item.getItemMeta() instanceof PotionMeta meta) {
                    var type = new BukkitPotionType(meta);
                    var typeString = type.getType().name().toLowerCase(Locale.ROOT);

                    if (!Version.isAtLeastVersion(20, 2)) {
                        if (type.isExtended()) {
                            typeString = "long_" + typeString;
                        } else if (type.isUpgraded()) {
                            typeString = "strong_" + typeString;
                        }
                    }

                    metas.add(meta(event.getBlock().getLocation(), typeString));
                } else if (!item.isEmpty()) {
                    metas.add(meta(event.getBlock().getLocation(), AuroraAPI.getItemManager().resolveId(item).toString()));
                }
            }

            player.getScheduler().run(AuroraQuests.getInstance(), (t2) -> {
                for (var meta : metas) {
                    progress(1, meta);
                }
            }, null);
        });
    }
}
