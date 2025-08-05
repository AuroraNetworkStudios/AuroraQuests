package gg.auroramc.quests.config.quest;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@ToString
public class FilterConfig {
    private SmartList worlds;
    private SmartList regions;
    private SmartList biomes;
    private Integer minYLevel;
    private Integer maxYLevel;
    private HandConfig hand;
    private List<String> requirements = new ArrayList<>();

    @Getter
    @ToString
    public static class SmartList {
        private String type;
        private Set<String> value;
    }

    @Getter
    @ToString
    public static class HandConfig {
        private Set<String> items;
    }
}
