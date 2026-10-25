package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class DamageItemObjective extends TypedObjective {

    public DamageItemObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerItemDamageEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerItemDamageEvent event) {
        var damage = event.getDamage();
        if (damage <= 0) return;
        progress(damage, meta(AuroraAPI.getItemManager().resolveId(event.getItem())));
    }
}
