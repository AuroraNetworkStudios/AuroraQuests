package gg.auroramc.quests.objective;

import com.google.common.collect.Lists;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.event.objective.PlayerKillMobEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.objective.filter.ObjectiveFilter;
import gg.auroramc.quests.api.objective.filter.RangeFilter;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;


public class LevelledMobKillObjective extends TypedObjective {

    public LevelledMobKillObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(EntityDeathEvent.class, this::handle, EventPriority.MONITOR);
        onEvent(PlayerKillMobEvent.class, this::onCustomMobKill, EventPriority.MONITOR);
    }

    public void handle(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player player = entity.getKiller();
        if (player == null) return;
        if (entity instanceof Player) return;
        if (player.hasMetadata("NPC")) return;

        var id = AuroraAPI.getEntityManager().resolveId(entity);
        if (id.namespace().equals("mythicmobs")) return;

        var level = getMobLevel(entity);
        if (level != null) {
            progress(1, RangeFilter.with(meta(id), "level", level));
        }
    }

    public void onCustomMobKill(PlayerKillMobEvent event) {
        if (!event.isLevelled()) return;
        progress(event.getAmount(), RangeFilter.with(meta(event.getMob()), "level", event.getLevel()));
    }

    @Override
    public List<ObjectiveFilter> getFilters() {
        return Lists.newArrayList(RangeFilter.rangeFilter(definition.getArgs(), "level"));
    }

    public static Double getMobLevel(LivingEntity livingEntity) {
        var levelledMobsPlugin = Bukkit.getPluginManager().getPlugin("LevelledMobs");
        if (levelledMobsPlugin == null) return null;
        var levelKey = new NamespacedKey(levelledMobsPlugin, "level");
        var level = livingEntity.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
        return level != null ? level.doubleValue() : null;
    }
}