package gg.auroramc.quests.objective;

import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public class InteractBlockObjective extends TypedObjective {
    private final Set<Action> actions;

    public InteractBlockObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
        this.actions = parseActions(definition.getArgs().getStringList("actions"),
                name -> AuroraQuests.logger().warning("Unknown action '" + name + "' in interact block objective with id: " + getId()));
    }

    static Set<Action> parseActions(List<String> names, Consumer<String> onInvalid) {
        if (names.isEmpty()) return EnumSet.of(Action.RIGHT_CLICK_BLOCK);

        var actions = EnumSet.noneOf(Action.class);
        for (var name : names) {
            try {
                actions.add(Action.valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                onInvalid.accept(name);
            }
        }
        return actions;
    }

    @Override
    protected void activate() {
        onEvent(PlayerInteractEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerInteractEvent event) {
        var action = event.getAction();
        if (!actions.contains(action)) return;
        // Clicks fire once per hand, physical interactions have no hand
        if (action != Action.PHYSICAL && event.getHand() != EquipmentSlot.HAND) return;

        var block = event.getClickedBlock();
        if (block == null) return;
        progress(1, meta(block.getLocation(), block.getType()));
    }
}
