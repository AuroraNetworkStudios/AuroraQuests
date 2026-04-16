package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.StringTypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import io.papermc.paper.event.block.VaultChangeStateEvent;
import org.bukkit.block.data.type.Vault;
import org.bukkit.event.EventPriority;

public final class UnlockVaultObjective extends StringTypedObjective {

    public UnlockVaultObjective(final Quest quest, final ObjectiveDefinition definition, final Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(VaultChangeStateEvent.class, this::onVaultStateChange, EventPriority.MONITOR);
    }

    public void onVaultStateChange(final VaultChangeStateEvent event) {
        if (event.getBlock().getState() instanceof org.bukkit.block.Vault vault && event.getPlayer() != null && event.getNewState() == Vault.State.UNLOCKING) {
            progress(1, meta(event.getBlock().getLocation(), vault.getLootTable().key().asString()));
        }
    }

}