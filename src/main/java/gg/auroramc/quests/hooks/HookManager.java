package gg.auroramc.quests.hooks;

import gg.auroramc.quests.AuroraQuests;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class HookManager {
    private static final Map<Class<? extends Hook>, Hook> hooks = new HashMap<>();

    public static void enableHooks(AuroraQuests plugin) {
        for (var hook : hooks.values()) {
            try {
                hook.hook(plugin);
                if (hook instanceof Listener) {
                    Bukkit.getPluginManager().registerEvents((Listener) hook, plugin);
                }
            } catch (Throwable e) {
                AuroraQuests.logger().warning("Failed to enable hook " + hook.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    public static void loadHooks(AuroraQuests plugin) {
        for (var hook : Hooks.values()) {
            try {
                if (hook.canHook()) {
                    var instance = hook.getClazz().getDeclaredConstructor().newInstance();
                    instance.hookAtStartUp(plugin);
                    hooks.put(hook.getClazz(), instance);
                }
            } catch (Throwable e) {
                AuroraQuests.logger().warning("Failed to hook " + String.join(", ", hook.getPlugins()) + ": " + e.getMessage());
            }
        }
    }

    public static <T extends Hook> T getHook(Class<T> clazz) {
        return clazz.cast(hooks.get(clazz));
    }

    public static <T extends Hook> boolean isEnabled(Class<T> clazz) {
        return hooks.get(clazz) != null;
    }
}
