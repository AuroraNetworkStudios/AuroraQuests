package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.event.objective.PlayerKillMobEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobKillObjective extends TypedObjective {
    private static final TypeId CHARGED_CREEPER = TypeId.fromString("minecraft:charged_creeper");
    private final boolean tracksCharged;

    public MobKillObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
        tracksCharged = definition.getArgs().getStringList("types").stream()
                .map(TypeId::fromString).anyMatch(CHARGED_CREEPER::equals);
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
        // Only quests that list charged_creeper see it; others keep seeing plain creeper
        if (tracksCharged && id.namespace().equals("minecraft") && entity instanceof Creeper c && c.isPowered()) id = CHARGED_CREEPER;

        progress(1, meta(id));
    }

    public void onCustomMobKill(PlayerKillMobEvent event) {
        progress(event.getAmount(), meta(event.getMob()));
    }
}
