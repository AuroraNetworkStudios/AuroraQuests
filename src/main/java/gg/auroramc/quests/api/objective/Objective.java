package gg.auroramc.quests.api.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.BukkitEventBus;
import gg.auroramc.quests.api.event.EventBus;
import gg.auroramc.quests.api.event.EventType;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import gg.auroramc.quests.api.objective.filter.ObjectiveFilter;
import gg.auroramc.quests.hooks.HookManager;
import gg.auroramc.quests.hooks.worldguard.WorldGuardHook;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
public abstract class Objective extends EventBus {
    protected final ObjectiveDefinition definition;
    protected final Quest quest;
    protected final double target;
    protected final Profile.TaskDataWrapper data;
    protected boolean started = false;

    @Getter(AccessLevel.NONE)
    protected List<BukkitEventBus.Subscription> subscriptions;
    @Getter(AccessLevel.NONE)
    protected List<ScheduledTask> tasks;

    protected List<ObjectiveFilter> filters;

    public Objective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        this.quest = quest;
        this.definition = definition;
        this.target = definition.getArgs().getDouble("amount", 1);
        this.data = data;
        this.filters = getFilters();
        if (this.filters == null) {
            this.filters = new ArrayList<>();
        }
    }

    protected <E extends Event> void onEvent(Class<E> event, Consumer<E> handler, EventPriority priority, boolean ignoreCancelled, boolean handleSubclass) {
        if (subscriptions == null) {
            subscriptions = new ArrayList<>();
        }
        subscriptions.add(AuroraQuests.getInstance().getBukkitEventBus().subscribe(event, (e) -> {
            if (e instanceof PlayerEvent playerEvent) {
                if (playerEvent.getPlayer() == data.profile().getPlayer()) {
                    handler.accept(e);
                }
            } else {
                handler.accept(e);
            }
        }, priority, ignoreCancelled, handleSubclass));
    }

    protected <E extends Event> void onEvent(Class<E> event, Consumer<E> handler, EventPriority priority) {
        this.onEvent(event, handler, priority, true, false);
    }

    protected <E extends Event> void onEvent(Class<E> event, Consumer<E> handler, EventPriority priority, boolean ignoreCancelled) {
        this.onEvent(event, handler, priority, ignoreCancelled, false);
    }

    protected void syncInterval(Runnable runnable, int delay, int interval, boolean global) {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }

        ScheduledTask task;

        if (global) {
            task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(AuroraQuests.getInstance(), (t) -> {
                runnable.run();
            }, delay, interval);
        } else {
            task = data.profile().getPlayer().getScheduler().runAtFixedRate(AuroraQuests.getInstance(), (t) -> {
                runnable.run();
            }, null, delay, interval);
        }

        tasks.add(task);
    }

    protected void asyncInterval(Runnable runnable, int delay, int interval) {
        var task = Bukkit.getAsyncScheduler().runAtFixedRate(AuroraQuests.getInstance(), (t) -> {
            runnable.run();
        }, delay * 50L, interval * 50L, TimeUnit.MILLISECONDS);

        tasks.add(task);
    }

    protected boolean passesFilters(ObjectiveMeta meta) {
        if (!meta.getPlayer().hasPermission("aurora.quests.use")) return false;

        if (HookManager.isEnabled(WorldGuardHook.class)) {
            if (HookManager.getHook(WorldGuardHook.class).isBlocked(meta.getPlayer(), meta.getLocation())) return false;
        }

        for (var filter : definition.getFilters()) {
            if (!filter.filter(meta)) return false;
        }

        for (var filter : filters) {
            if (!filter.filter(meta)) return false;
        }

        return true;
    }

    public String getId() {
        return definition.getId();
    }

    public void progress(double progress, ObjectiveMeta meta) {
        if (isCompleted()) return;

        if (passesFilters(meta)) {
            data.progress(progress);
            this.publish(EventType.TASK_PROGRESS, this);
            if (isCompleted()) {
                this.publish(EventType.TASK_COMPLETED, this);
                dispose();
            }
        }
    }

    public void start() {
        if (this.started) return;
        if (isCompleted()) return;
        this.activate();
        started = true;
    }

    protected abstract void activate();

    public List<ObjectiveFilter> getFilters() {
        return new ArrayList<>();
    }

    public void dispose() {
        if (subscriptions != null) {
            for (var subscription : subscriptions) {
                subscription.unsubscribe();
            }
            subscriptions = null;
        }

        if (tasks != null) {
            for (var task : tasks) {
                task.cancel();
            }
            tasks = null;
        }
        started = false;
    }

    public void destroy() {
        super.dispose();
        dispose();
    }

    public double getProgress() {
        return data.isCompleted(target) ? target : data.getProgress();
    }

    public boolean isCompleted() {
        return data.isCompleted(target);
    }

    public String getType() {
        return definition.getTask();
    }

    public void complete(boolean silent) {
        data.setProgress(target);
        dispose();
        if (!silent) this.publish(EventType.TASK_COMPLETED, this);
    }

    public void setProgress(double progress) {
        data.setProgress(Math.min(progress, target));
        this.publish(EventType.TASK_PROGRESS, this);
        if (isCompleted()) {
            dispose();
            this.publish(EventType.TASK_COMPLETED, this);
        }
    }

    public void resetProgress() {
        data.resetProgress();
        this.publish(EventType.TASK_PROGRESS, this);
    }

    public String display() {
        var gc = AuroraQuests.getInstance().getConfigManager().getCommonMenuConfig().getTaskStatuses();
        var count = isCompleted() ? target : Math.min(data.getProgress(), target);

        return Placeholder.execute(definition.getDisplay(),
                Placeholder.of("{status}", isCompleted() ? gc.getCompleted() : gc.getNotCompleted()),
                Placeholder.of("{current}", AuroraAPI.formatNumber(count)),
                Placeholder.of("{required}", AuroraAPI.formatNumber(target))
        );
    }

    protected ObjectiveMeta meta() {
        return new ObjectiveMeta(data.profile().getPlayer(), data.profile().getPlayer().getLocation());
    }

    protected ObjectiveMeta meta(Location location) {
        return new ObjectiveMeta(data.profile().getPlayer(), location);
    }

}
