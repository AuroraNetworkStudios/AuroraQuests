package gg.auroramc.quests.api.event.objective;

import gg.auroramc.aurora.api.item.TypeId;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerInteractNpcEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    private final TypeId npc;
    private final InteractionType interactionType;

    public PlayerInteractNpcEvent(@NotNull Player who, TypeId npc, InteractionType interactionType) {
        super(who, !Bukkit.isPrimaryThread());
        this.npc = npc;
        this.interactionType = interactionType;
    }

    public PlayerInteractNpcEvent(@NotNull Player who, TypeId npc) {
        this(who, npc, InteractionType.UNKNOWN);
    }


    public enum InteractionType {
        LEFT_CLICK,
        RIGHT_CLICK,
        UNKNOWN
    }
}
