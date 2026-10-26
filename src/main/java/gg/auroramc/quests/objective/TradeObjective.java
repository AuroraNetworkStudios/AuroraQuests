package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventPriority;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class TradeObjective extends TypedObjective {
    private final Set<String> villagerTypes;
    private final Set<String> professions;

    public TradeObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
        this.villagerTypes = normalize(definition.getArgs().getStringList("villager-types"));
        this.professions = normalize(definition.getArgs().getStringList("professions"));
    }

    static Set<String> normalize(List<String> names) {
        return names.stream()
                .map(name -> name.toLowerCase(Locale.ROOT).replaceFirst("^minecraft:", ""))
                .collect(Collectors.toSet());
    }

    @Override
    protected void activate() {
        warnUnknown("villager-types", villagerTypes, Registry.VILLAGER_TYPE);
        warnUnknown("professions", professions, Registry.VILLAGER_PROFESSION);
        // Villagers and wandering traders only; plugin merchant GUIs fire the parent PlayerPurchaseEvent
        onEvent(PlayerTradeEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerTradeEvent event) {
        if (!matchesVillager(event.getVillager())) return;
        var result = event.getTrade().getResult();
        progress(result.getAmount(), meta(event.getVillager().getLocation(), AuroraAPI.getItemManager().resolveId(result)));
    }

    private boolean matchesVillager(AbstractVillager merchant) {
        if (villagerTypes.isEmpty() && professions.isEmpty()) return true;
        // Wandering traders have neither a type nor a profession
        if (!(merchant instanceof Villager villager)) return false;
        return (villagerTypes.isEmpty() || villagerTypes.contains(villager.getVillagerType().key().value()))
                && (professions.isEmpty() || professions.contains(villager.getProfession().key().value()));
    }

    private void warnUnknown(String arg, Set<String> names, Registry<? extends Keyed> registry) {
        for (var name : names) {
            if (registry.stream().noneMatch(entry -> entry.key().value().equals(name))) {
                AuroraQuests.logger().warning("Unknown " + arg + " value '" + name + "' in trade objective with id: " + getId());
            }
        }
    }
}
