package gg.auroramc.quests.api.event.objective;

import gg.auroramc.aurora.api.item.TypeId;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerCaughtFishEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    private final TypeId type;
    private final int amount;
    private final Location location;

    public PlayerCaughtFishEvent(@NotNull Player who, TypeId type, int amount, Location location) {
        super(who, !Bukkit.isPrimaryThread());
        this.type = type;
        this.amount = amount;
        this.location = location;
    }
}
