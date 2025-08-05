package gg.auroramc.quests.api.event;

import gg.auroramc.quests.api.objective.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventBus {
    private final Map<EventType, List<Consumer<Objective>>> subscribers = new HashMap<>();

    public void subscribe(EventType eventType, Consumer<Objective> handler) {
        List<Consumer<Objective>> list = subscribers.computeIfAbsent(eventType, k -> new ArrayList<>());
        list.add(handler);
    }

    public boolean unsubscribe(EventType eventType, Consumer<Objective> handler) {
        List<Consumer<Objective>> list = subscribers.get(eventType);
        if (list == null) return false;
        boolean removed = list.remove(handler);
        if (removed && list.isEmpty()) {
            subscribers.remove(eventType);
        }
        return removed;
    }

    public void publish(EventType eventType, Objective task) {
        List<Consumer<Objective>> list = subscribers.get(eventType);
        if (list == null) return;

        for (Consumer<Objective> handler : list) {
            try {
                handler.accept(task);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void dispose() {
        subscribers.clear();
    }
}
