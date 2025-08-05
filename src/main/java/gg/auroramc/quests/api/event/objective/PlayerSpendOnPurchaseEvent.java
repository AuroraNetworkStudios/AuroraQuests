package gg.auroramc.quests.api.event.objective;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class PlayerSpendOnPurchaseEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    private final double amount;
    private final @Nullable String currency;

    public PlayerSpendOnPurchaseEvent(@NotNull Player who, double amount, @Nullable String currency) {
        super(who, !Bukkit.isPrimaryThread());
        this.amount = amount;
        this.currency = currency;
    }

    public PlayerSpendOnPurchaseEvent(@NotNull Player who, double amount) {
        this(who, amount, null);
    }
}
