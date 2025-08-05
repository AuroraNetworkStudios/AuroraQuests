package gg.auroramc.quests.api.event.objective;

import gg.auroramc.quests.api.quest.Quest;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerTakeItemEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    private final Quest quest;

    public PlayerTakeItemEvent(@NotNull Player who, Quest quest) {
        super(who, !Bukkit.isPrimaryThread());
        this.quest = quest;
    }
}
