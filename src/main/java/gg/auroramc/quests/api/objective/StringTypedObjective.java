package gg.auroramc.quests.api.objective;

import gg.auroramc.quests.api.objective.filter.StringTypeFilter;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.Location;

import java.util.HashSet;

public abstract class StringTypedObjective extends Objective {

    public StringTypedObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
        var filter = new StringTypeFilter(
                new HashSet<>(definition.getArgs().getStringList("types")),
                StringTypeFilter.Mode.parse(definition.getArgs().getString("mode", "whitelist"))
        );
        this.filters.add(filter);
    }

    protected ObjectiveMeta meta(Location location, String type) {
        var meta = new ObjectiveMeta(data.profile().getPlayer(), location);
        meta.setVariable("type", type);
        return meta;
    }

    protected ObjectiveMeta meta(String type) {
        return meta(data.profile().getPlayer().getLocation(), type);
    }
}