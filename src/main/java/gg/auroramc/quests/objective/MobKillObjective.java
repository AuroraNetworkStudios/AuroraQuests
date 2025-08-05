package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.event.objective.PlayerKillMobEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobKillObjective extends TypedObjective {

    public MobKillObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(EntityDeathEvent.class, this::handle, EventPriority.MONITOR);
        onEvent(PlayerKillMobEvent.class, this::onCustomMobKill, EventPriority.MONITOR);
    }

    public void handle(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getKiller() != data.profile().getPlayer()) return;
        if (entity instanceof Player) return;

        var id = AuroraAPI.getEntityManager().resolveId(entity);
        if (id.namespace().equals("mythicmobs")) return;

        progress(1, meta(id));
    }

    public void onCustomMobKill(PlayerKillMobEvent event) {
        progress(event.getAmount(), meta(event.getMob()));
    }
}
