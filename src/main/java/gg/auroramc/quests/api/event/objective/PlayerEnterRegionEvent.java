package gg.auroramc.quests.api.event.objective;

import gg.auroramc.aurora.api.item.TypeId;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
public class PlayerEnterRegionEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    private final Set<TypeId> regions;

    public PlayerEnterRegionEvent(@NotNull Player who, Set<TypeId> regions) {
        super(who, !Bukkit.isPrimaryThread());
        this.regions = regions;
    }

    public PlayerEnterRegionEvent(@NotNull Player who, TypeId region) {
        super(who, !Bukkit.isPrimaryThread());
        this.regions = Set.of(region);
    }
}
