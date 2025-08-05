package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.event.objective.PlayerLootEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityLootObjective extends TypedObjective {

    public EntityLootObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(EntityDeathEvent.class, this::handle, EventPriority.MONITOR);
        onEvent(PlayerLootEvent.class, this::handleCustomLoot, EventPriority.MONITOR);
    }

    public void handle(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getKiller() != data.profile().getPlayer()) return;
        if (entity instanceof Player) return;

        var id = AuroraAPI.getEntityManager().resolveId(entity);
        if (id.namespace().equals("mythicmobs")) return;

        for (var drop : event.getDrops()) {
            var typeId = AuroraAPI.getItemManager().resolveId(drop);
            progress(drop.getAmount(), meta(typeId));
        }
    }

    public void handleCustomLoot(PlayerLootEvent event) {
        if (event.getSource() == PlayerLootEvent.Source.ENTITY || event.getSource() == PlayerLootEvent.Source.ALL) {
            progress(event.getAmount(), meta(event.getType()));
        }
    }
}