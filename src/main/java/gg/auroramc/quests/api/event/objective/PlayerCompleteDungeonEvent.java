package gg.auroramc.quests.api.event.objective;

import gg.auroramc.aurora.api.item.TypeId;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerCompleteDungeonEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    private final TypeId dungeon;
    private final String difficulty;

    public PlayerCompleteDungeonEvent(@NotNull Player who, TypeId dungeon, String difficulty) {
        super(who, !Bukkit.isPrimaryThread());
        this.dungeon = dungeon;
        this.difficulty = difficulty;
    }
}
