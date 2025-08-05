package gg.auroramc.quests.api.event.objective;

import gg.auroramc.aurora.api.item.TypeId;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerKillMobEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    private final TypeId mob;
    private final int amount;
    private final double level;

    public PlayerKillMobEvent(@NotNull Player who, TypeId mob, int amount, double level) {
        super(who, !Bukkit.isPrimaryThread());
        this.mob = mob;
        this.amount = amount;
        this.level = level;
    }

    public PlayerKillMobEvent(@NotNull Player who, TypeId mob) {
        this(who, mob, 1, -1);
    }

    public PlayerKillMobEvent(@NotNull Player who, TypeId mob, int amount) {
        this(who, mob, amount, -1);
    }

    public PlayerKillMobEvent(@NotNull Player who, TypeId mob, double level) {
        this(who, mob, 1, level);
    }

    public boolean isLevelled() {
        return level != -1;
    }
}
