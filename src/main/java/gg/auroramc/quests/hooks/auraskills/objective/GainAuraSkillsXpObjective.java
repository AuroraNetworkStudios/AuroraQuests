package gg.auroramc.quests.hooks.auraskills.objective;

import dev.aurelium.auraskills.api.event.skill.XpGainEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.StringTypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class GainAuraSkillsXpObjective extends StringTypedObjective {

    public GainAuraSkillsXpObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(XpGainEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(XpGainEvent e) {
        if (e.getPlayer() != data.profile().getPlayer()) return;

        var xp = e.getAmount();
        var skill = e.getSkill().getId().toString();

        progress(xp, meta(skill));
    }
}
