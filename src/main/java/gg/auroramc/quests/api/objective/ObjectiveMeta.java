package gg.auroramc.quests.api.objective;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class ObjectiveMeta {
    private final Player player;
    private final Location location;

    @Getter(AccessLevel.NONE)
    private Map<String, Object> variables;

    public ObjectiveMeta(Player player, Location location) {
        this.player = player;
        this.location = location;
    }

    public ObjectiveMeta(Player player) {
        this(player, player.getLocation());
    }

    /**
     * Stores a variable of type T with the given name.
     */
    public <T> void setVariable(String name, T value) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put(name, value);
    }

    /**
     * Retrieve a variable by name, ensuring it's of the expected type.
     *
     * @param name the variable name
     * @param type the Class token for the expected type
     * @return an Optional containing the value if present and of the correct type
     */
    public <T> Optional<T> getVariable(String name, Class<T> type) {
        if (variables == null) {
            return Optional.empty();
        }

        Object raw = variables.get(name);
        if (type.isInstance(raw)) {
            return Optional.of(type.cast(raw));
        }

        return Optional.empty();
    }
}
