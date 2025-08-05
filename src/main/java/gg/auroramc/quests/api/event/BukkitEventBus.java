package gg.auroramc.quests.api.event;

import gg.auroramc.quests.AuroraQuests;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class BukkitEventBus implements Listener {
    private final ConcurrentLinkedQueue<Listener> queue = new ConcurrentLinkedQueue<>();
    private final ConcurrentMap<EventPriority, Set<Class<? extends Event>>> events = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends Event>,
            ConcurrentMap<EventPriority, CopyOnWriteArrayList<Consumer<? extends Event>>>> subscribers
            = new ConcurrentHashMap<>();

    public <E extends Event> Subscription subscribe(
            Class<E> eventType,
            Consumer<E> handler,
            EventPriority priority,
            boolean ignoreCancelled,
            boolean handleSubclass
    ) {
        // atomically register with Bukkit exactly once
        events.compute(priority, (prio, set) -> {
            if (set == null) {
                set = ConcurrentHashMap.newKeySet();
            }
            boolean wasAdded = set.add(eventType);

            if (wasAdded) {
                Bukkit.getPluginManager().registerEvent(
                        eventType, this, priority,
                        (lst, evt) -> {
                            if (handleSubclass) {
                                if (!eventType.isInstance(evt)) {
                                    return;
                                }
                            } else if (evt.getClass() != eventType) {
                                return;
                            }
                            dispatch(priority, evt);
                        },
                        AuroraQuests.getInstance(), ignoreCancelled
                );

            }
            return set;
        });

        // register handler, but only once
        var prMap = subscribers.computeIfAbsent(
                eventType, k -> new ConcurrentHashMap<>()
        );
        var handlers = prMap.computeIfAbsent(
                priority, k -> new CopyOnWriteArrayList<>()
        );

        boolean handlerExists = handlers.contains(handler);

        if (!handlerExists) {
            handlers.add(handler);
        }

        return new Subscription(this, eventType, handler, priority);
    }

    @SuppressWarnings("unchecked")
    private void dispatch(EventPriority priority, Event event) {
        var prMap = subscribers.get(event.getClass());
        if (prMap == null) {
            return;
        }

        var handlers = prMap.get(priority);
        if (handlers == null) {
            return;
        }

        for (Consumer<? extends Event> h : handlers) {
            ((Consumer<Event>) h).accept(event);
        }
    }


    public <E extends Event> void unsubscribe(Class<? extends Event> eventType, Consumer<E> handler, EventPriority priority) {
        var prMap = subscribers.get(eventType);
        if (prMap == null) return;

        var handlers = prMap.get(priority);
        if (handlers == null) return;

        handlers.remove(handler);
        if (handlers.isEmpty()) {
            prMap.remove(priority);
            if (prMap.isEmpty()) {
                subscribers.remove(eventType);
            }
        }
    }

    @RequiredArgsConstructor
    public static final class Subscription {
        private final BukkitEventBus bukkitEventBus;
        private final Class<? extends Event> eventType;
        private final Consumer<? extends Event> handler;
        private final EventPriority priority;

        public void unsubscribe() {
            bukkitEventBus.unsubscribe(eventType, handler, priority);
        }
    }
}
