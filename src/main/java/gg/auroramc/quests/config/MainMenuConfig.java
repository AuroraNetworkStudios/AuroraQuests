package gg.auroramc.quests.config;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;

import java.util.Map;

@Getter
public class MainMenuConfig {
    private String title;
    private Integer menuRows = 6;
    private FillerConfig filler;
    private Boolean hasCloseButton = true;
    private Map<String, ItemConfig> items;
    private Map<String, ItemConfig> customItems;

    @Getter
    public static class FillerConfig {
        private Boolean enabled = false;
        private ItemConfig item;
    }
}
