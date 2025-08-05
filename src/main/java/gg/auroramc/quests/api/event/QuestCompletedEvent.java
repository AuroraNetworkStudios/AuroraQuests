package gg.auroramc.quests.api.event;

import gg.auroramc.quests.api.quest.Quest;
import gg.auroramc.quests.api.questpool.QuestPool;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class QuestCompletedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    private final Player player;
    private final QuestPool pool;
    private final Quest quest;

    public QuestCompletedEvent(Player player, QuestPool pool, Quest quest) {
        super(!Bukkit.isPrimaryThread());
        this.player = player;
        this.pool = pool;
        this.quest = quest;
    }
}
