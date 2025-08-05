package gg.auroramc.quests.config.quest;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TaskConfig {
    private String task;
    private String display;
    private ConfigurationSection args;
    private FilterConfig filters;
    private List<String> onProgress = new ArrayList<>();
    private List<String> onComplete = new ArrayList<>();
}
