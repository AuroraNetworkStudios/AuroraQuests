package gg.auroramc.quests.api.event;

import gg.auroramc.quests.api.objective.Objective;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class PlayerTrackedObjectiveChangeEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Nullable
    private final Objective previousObjective;
    @Setter
    @Nullable
    private Objective newObjective;
    @Setter
    private boolean cancelled;

    public PlayerTrackedObjectiveChangeEvent(Player player, @Nullable Objective previousObjective, @Nullable Objective newObjective) {
        super(player, !Bukkit.isPrimaryThread());
        this.previousObjective = previousObjective;
        this.newObjective = newObjective;
    }
}
