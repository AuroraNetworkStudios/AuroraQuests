package gg.auroramc.quests.menu;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.config.CommonMenuConfig;
import gg.auroramc.quests.config.MainMenuConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MainMenu {
    private final Profile profile;
    private int page = 1;
    private int maxPage = 1;

    public MainMenu(Profile profile) {
        this.profile = profile;
    }

    public void open() {
        createMenu().open();
    }

    private ItemConfig merge(CommonMenuConfig cmf, MainMenuConfig mc, String key) {
        return cmf.getItems().get(key).merge(mc.getItems().get(key));
    }

    private AuroraMenu createMenu() {
        var localization = AuroraQuests.getInstance().getLocalizationProvider();

        var config = AuroraQuests.getInstance().getConfigManager().getMainMenuConfig();
        var cmf = AuroraQuests.getInstance().getConfigManager().getCommonMenuConfig();
        var player = profile.getPlayer();

        var menu = new AuroraMenu(player, config.getTitle(), config.getMenuRows() * 9, false, localization);

        if (config.getFiller().getEnabled()) {
            menu.addFiller(ItemBuilder.of(config.getFiller().getItem()).localization(localization).toItemStack(player));
        } else {
            menu.addFiller(ItemBuilder.filler(Material.AIR));
        }

        if (config.getHasCloseButton()) {
            menu.addItem(ItemBuilder.close(merge(cmf, config, "close")).localization(localization).build(player), (e) -> {
                player.closeInventory();
            });
        }

        var pools = profile.getQuestPools();
        var maybeInt = pools.stream()
                .filter(pool -> pool.getDefinition().getMenuItem().getShowInMainMenu())
                .filter(pool -> pool.isUnlocked() || pool.getDefinition().getRequirement().isAlwaysShowInMenu())
                .mapToInt(pool -> pool.getDefinition().getMenuItem().getPage()).max();

        if (maybeInt.isPresent()) {
            maxPage = maybeInt.getAsInt();
        }

        var user = AuroraAPI.getUser(player.getUniqueId());
        var lbm = AuroraAPI.getLeaderboards();


        for (var pool : pools) {
            if (!pool.isUnlocked() && !pool.getDefinition().getRequirement().isAlwaysShowInMenu()) continue;
            var mi = pool.getDefinition().getMenuItem();
            if (!mi.getShowInMainMenu()) continue;
            if (mi.getPage() != page) continue;

            var boardName = "quests_" + pool.getId();

            List<Placeholder<?>> placeholders = new ArrayList<>();
            var lb = user.getLeaderboardEntries().get(boardName);

            if (lb != null && lb.getPosition() != 0) {
                placeholders.add(Placeholder.of("{lb_position}", AuroraAPI.formatNumber(lb.getPosition())));
                placeholders.add(Placeholder.of("{lb_position_percent}", AuroraAPI.formatNumber(
                        Math.min(((double) lb.getPosition() / Math.max(1, AuroraAPI.getLeaderboards().getBoardSize(boardName))) * 100, 100)
                )));
                placeholders.add(Placeholder.of("{lb_size}",
                        AuroraAPI.formatNumber(
                                Math.max(Math.max(lb.getPosition(), Bukkit.getOnlinePlayers().size()), AuroraAPI.getLeaderboards().getBoardSize(boardName)))));
            } else {
                placeholders.add(Placeholder.of("{lb_position}", lbm.getEmptyPlaceholder()));
                placeholders.add(Placeholder.of("{lb_position_percent}", lbm.getEmptyPlaceholder()));
                placeholders.add(Placeholder.of("{lb_size}",
                        AuroraAPI.formatNumber(Math.max(Bukkit.getOnlinePlayers().size(), AuroraAPI.getLeaderboards().getBoardSize(boardName)))));
            }

            var lore = new ArrayList<String>();

            if (!pool.isUnlocked()) {
                lore.addAll(mi.getLockedLore());
            }

            menu.addItem(ItemBuilder.of(mi.getItem())
                    .setName(Placeholder.execute(mi.getItem().getName(), Placeholder.of("{name}", pool.getDefinition().getName())))
                    .localization(localization)
                    .extraLore(lore)
                    .placeholder(Placeholder.of("{name}", pool.getDefinition().getName()))
                    .placeholder(Placeholder.of("{total_completed}", AuroraAPI.formatNumber(pool.getCompletedQuestCount())))
                    .placeholder(placeholders)
                    .build(player), (e) -> {
                if (pool.isUnlocked()) {
                    new PoolMenu(profile, pool).open();
                }
            });
        }

        for (var customItem : config.getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem).localization(localization).build(player));
        }

        if (maxPage > 1) {
            // Add pagination items
            List<Placeholder<?>> placeholders = List.of(
                    Placeholder.of("{current}", page),
                    Placeholder.of("{total}", maxPage)
            );

            menu.addItem(ItemBuilder.of(merge(cmf, config, "previous-page")).localization(localization).placeholder(placeholders).build(player), (e) -> {
                if (page > 1) {
                    page--;
                    createMenu().open();
                }
            });

            menu.addItem(ItemBuilder.of(merge(cmf, config, "current-page")).localization(localization).placeholder(placeholders).build(player));

            menu.addItem(ItemBuilder.of(merge(cmf, config, "next-page")).localization(localization).placeholder(placeholders).build(player), (e) -> {
                if (page < maxPage) {
                    page++;
                    createMenu().open();
                }
            });
        }

        return menu;
    }
}
