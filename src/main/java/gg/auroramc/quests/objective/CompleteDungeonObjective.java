package gg.auroramc.quests.objective;

import com.google.common.collect.Lists;
import gg.auroramc.quests.api.event.objective.PlayerCompleteDungeonEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.objective.filter.ObjectiveFilter;
import gg.auroramc.quests.api.objective.filter.StringFilter;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

import java.util.List;

public class CompleteDungeonObjective extends TypedObjective {

    public CompleteDungeonObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerCompleteDungeonEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerCompleteDungeonEvent event) {
        progress(1, StringFilter.with(meta(event.getDungeon()), "difficulty", event.getDifficulty()));
    }

    @Override
    public List<ObjectiveFilter> getFilters() {
        return Lists.newArrayList(StringFilter.stringFilter(definition.getArgs(), "difficulty"));
    }
}
