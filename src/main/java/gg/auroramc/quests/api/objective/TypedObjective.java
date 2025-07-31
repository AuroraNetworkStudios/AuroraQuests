package gg.auroramc.quests.api.objective;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.objective.filter.TypeFilter;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.stream.Collectors;

public abstract class TypedObjective extends Objective {

    public TypedObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
        var filter = new TypeFilter(
                definition.getArgs().getStringList("types").stream().map(TypeId::fromString).collect(Collectors.toSet()),
                TypeFilter.Mode.parse(definition.getArgs().getString("mode", "whitelist"))
        );
        this.filters.add(filter);
    }

    protected ObjectiveMeta meta(Location location, TypeId type) {
        var meta = new ObjectiveMeta(data.profile().getPlayer(), location);
        meta.setVariable("type", type);
        return meta;
    }

    protected ObjectiveMeta meta(Location location, String type) {
        var meta = new ObjectiveMeta(data.profile().getPlayer(), location);
        meta.setVariable("type", TypeId.fromString(type));
        return meta;
    }

    protected ObjectiveMeta meta(Location location, Material type) {
        var meta = new ObjectiveMeta(data.profile().getPlayer(), location);
        meta.setVariable("type", TypeId.from(type));
        return meta;
    }

    protected ObjectiveMeta meta(Location location, EntityType type) {
        var meta = new ObjectiveMeta(data.profile().getPlayer(), location);
        meta.setVariable("type", TypeId.from(type));
        return meta;
    }

    protected ObjectiveMeta meta(TypeId type) {
        return meta(data.profile().getPlayer().getLocation(), type);
    }

    protected ObjectiveMeta meta(String type) {
        return meta(data.profile().getPlayer().getLocation(), type);
    }

    protected ObjectiveMeta meta(Material type) {
        return meta(data.profile().getPlayer().getLocation(), type);
    }

    protected ObjectiveMeta meta(EntityType type) {
        return meta(data.profile().getPlayer().getLocation(), type);
    }
}
