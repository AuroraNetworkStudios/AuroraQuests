package gg.auroramc.quests.menu;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.menu.AuroraMenu;
import gg.auroramc.aurora.api.menu.ItemBuilder;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.objective.PlayerTakeItemEvent;
import gg.auroramc.quests.api.objective.ObjectiveType;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import gg.auroramc.quests.api.questpool.QuestPool;
import gg.auroramc.quests.util.RomanNumber;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class PoolMenu {
    private final Profile profile;
    private final QuestPool pool;
    private int page = 0;
    private boolean isCompletedQuests = false;
    private final Runnable backAction;

    public PoolMenu(Profile profile, QuestPool pool, @Nullable Runnable backAction) {
        this.profile = profile;
        this.pool = pool;
        this.backAction = backAction;
    }

    public PoolMenu(Profile profile, QuestPool pool) {
        this(profile, pool, null);
    }

    public void open() {
        createMenu().open();
    }

    private AuroraMenu createMenu() {
        var localization = AuroraQuests.getInstance().getLocalizationProvider();

        var config = pool.getPool().getDefinition();
        var mc = config.getMenu();
        var cmf = AuroraQuests.getInstance().getConfigManager().getCommonMenuConfig();
        var player = profile.getPlayer();


        var menu = new AuroraMenu(player, mc.getTitle(), mc.getRows() * 9, false, localization, Placeholder.of("{name}", config.getName()));

        if (config.getMenu().getFiller().getEnabled()) {
            menu.addFiller(ItemBuilder.of(mc.getFiller().getItem()).localization(localization).toItemStack(player));
        } else {
            menu.addFiller(ItemBuilder.filler(Material.AIR));
        }

        // Custom items
        var totalCompletedPlaceholder = Placeholder.of("{total_completed}", AuroraAPI.formatNumber(pool.getCompletedQuestCount()));
        var levelPlaceholder = Placeholder.of("{level}", AuroraAPI.formatNumber(pool.getLevel()));
        var levelRawPlaceholder = Placeholder.of("{level_raw}", pool.getLevel());
        var levelRomanPlaceholder = Placeholder.of("{level_roman}", RomanNumber.toRoman(pool.getLevel()));

        var lbm = AuroraAPI.getLeaderboards();
        var boardName = "quests_" + pool.getId();

        var user = AuroraAPI.getUser(player.getUniqueId());
        List<Placeholder<?>> lbPlaceholders = new ArrayList<>();
        var lb = user.getLeaderboardEntries().get(boardName);

        if (lb != null && lb.getPosition() != 0) {
            lbPlaceholders.add(Placeholder.of("{lb_position}", AuroraAPI.formatNumber(lb.getPosition())));
            lbPlaceholders.add(Placeholder.of("{lb_position_percent}", AuroraAPI.formatNumber(
                    Math.min(((double) lb.getPosition() / Math.max(1, AuroraAPI.getLeaderboards().getBoardSize(boardName))) * 100, 100)
            )));
            lbPlaceholders.add(Placeholder.of("{lb_size}",
                    AuroraAPI.formatNumber(
                            Math.max(Math.max(lb.getPosition(), Bukkit.getOnlinePlayers().size()), AuroraAPI.getLeaderboards().getBoardSize(boardName)))));
        } else {
            lbPlaceholders.add(Placeholder.of("{lb_position}", lbm.getEmptyPlaceholder()));
            lbPlaceholders.add(Placeholder.of("{lb_position_percent}", lbm.getEmptyPlaceholder()));
            lbPlaceholders.add(Placeholder.of("{lb_size}",
                    AuroraAPI.formatNumber(Math.max(Bukkit.getOnlinePlayers().size(), AuroraAPI.getLeaderboards().getBoardSize(boardName)))));
        }

        for (var customItem : config.getMenu().getCustomItems().values()) {
            menu.addItem(ItemBuilder.of(customItem)
                    .localization(localization)
                    .placeholder(totalCompletedPlaceholder)
                    .placeholder(Placeholder.of("{name}", config.getName()))
                    .placeholder(levelPlaceholder)
                    .placeholder(levelRawPlaceholder)
                    .placeholder(levelRomanPlaceholder)
                    .placeholder(lbPlaceholders)
                    .build(player));
        }

        // Close and back buttons
        if (mc.getHasCloseButton()) {
            var closeConfig = cmf.getItems().get("close").merge(mc.getItems().get("close"));

            menu.addItem(ItemBuilder.close(closeConfig).localization(localization).build(player), (e) -> {
                player.closeInventory();
            });
        }

        if (mc.getHasBackButton()) {
            var backConfig = cmf.getItems().get("back").merge(mc.getItems().get("back"));

            menu.addItem(ItemBuilder.back(backConfig).localization(localization).build(player), (e) -> {
                if (backAction != null) {
                    backAction.run();
                } else {
                    new MainMenu(profile).open();
                }
            });
        }

        // Display quests
        var quests = getPage(page, mc.getDisplayArea().size());

        for (int i = 0; i < mc.getDisplayArea().size(); i++) {
            var slot = mc.getDisplayArea().get(i);
            if (quests.size() <= i) {
                menu.addItem(ItemBuilder.item(new ItemStack(Material.AIR)).slot(slot).build(player));
                continue;
            }

            var quest = quests.get(i);
            var extraLore = new ArrayList<String>();

            if (quest.isCompleted()) {
                extraLore.addAll(quest.getDefinition().getCompletedLore());
            } else {
                if (quest.getDefinition().getUncompletedLore() != null) {
                    extraLore.addAll(quest.getDefinition().getUncompletedLore());
                }
            }

            if (!quest.isUnlocked() && pool.isGlobal()) {
                extraLore.addAll(quest.getDefinition().getLockedLore());
            }

            var builder = ItemBuilder.of(quest.getDefinition().getMenuItem()).slot(slot)
                    .setName(Placeholder.execute(quest.getDefinition().getMenuItem().getName(), Placeholder.of("{name}", config.getName()) ))
                    .localization(localization)
                    .placeholder(quest.getPlaceholders()).extraLore(extraLore);

            if ((quest.isUnlocked() || !pool.isGlobal()) && !quest.isCompleted() && quest.getDefinition().getTasks().values().stream().anyMatch(t -> t.getTask().equals(ObjectiveType.TAKE_ITEM))) {
                menu.addItem(builder.build(player), (e) -> {
                    Bukkit.getPluginManager().callEvent(new PlayerTakeItemEvent(player, quest));
                    createMenu().open(player);
                });
            } else {
                menu.addItem(builder.build(player));
            }
        }

        // Pagination
        if (getQuests().size() > mc.getDisplayArea().size()) {
            var pageCount = getTotalPageCount(mc.getDisplayArea().size());
            List<Placeholder<?>> placeholders = List.of(Placeholder.of("{current}", page + 1), Placeholder.of("{max}", pageCount + 1));

            menu.addItem(ItemBuilder.of(cmf.getItems().get("previous-page").merge(mc.getItems().get("previous-page")))
                    .localization(localization)
                    .placeholder(placeholders).build(player), (e) -> {
                if (page > 0) {
                    page--;
                    createMenu().open();
                }
            });

            menu.addItem(ItemBuilder.of(cmf.getItems().get("current-page").merge(mc.getItems().get("current-page")))
                    .localization(localization)
                    .placeholder(placeholders).build(player));

            menu.addItem(ItemBuilder.of(cmf.getItems().get("next-page").merge(mc.getItems().get("next-page")))
                    .localization(localization)
                    .placeholder(placeholders).build(player), (e) -> {
                if (page < pageCount) {
                    page++;
                    createMenu().open();
                }
            });
        }

        // Switch between completed/locked quests
        if (pool.isGlobal()) {
            if (isCompletedQuests) {
                var item = ItemBuilder.of(cmf.getItems().get("switch-to-active").merge(mc.getItems().get("switch-to-active")))
                        .localization(localization)
                        .placeholder(Placeholder.of("{name}", config.getName())).build(player);

                menu.addItem(item, (e) -> {
                    isCompletedQuests = false;
                    page = 0;
                    createMenu().open();
                });
            } else {
                var item = ItemBuilder.of(cmf.getItems().get("switch-to-completed").merge(mc.getItems().get("switch-to-completed")))
                        .localization(localization)
                        .placeholder(Placeholder.of("{name}", config.getName()))
                        .placeholder(totalCompletedPlaceholder)
                        .build(player);

                menu.addItem(item, (e) -> {
                    isCompletedQuests = true;
                    page = 0;
                    createMenu().open();
                });
            }

        }

        // Leveling button
        if (pool.hasLeveling()) {
            var item = ItemBuilder.of(cmf.getItems().get("switch-to-levels").merge(mc.getItems().get("switch-to-levels")))
                    .localization(localization)
                    .placeholder(Placeholder.of("{name}", config.getName()))
                    .placeholder(totalCompletedPlaceholder)
                    .placeholder(levelPlaceholder)
                    .placeholder(levelRawPlaceholder)
                    .placeholder(levelRomanPlaceholder)
                    .placeholder(lbPlaceholders)
                    .build(player);

            menu.addItem(item, (e) -> {
                if (backAction != null) {
                    new LevelMenu(profile, pool, () -> new PoolMenu(profile, pool, backAction).open()).open();
                } else {
                    new LevelMenu(profile, pool, null).open();
                }
            });
        }


        return menu;
    }

    private Collection<Quest> getQuests() {
        var gc = AuroraQuests.getInstance().getConfigManager().getConfig();
        Collection<Quest> quests;
        if (pool.isGlobal()) {
            if (isCompletedQuests) {
                quests = pool.getQuests().stream()
                        .filter(Quest::isCompleted)
                        .sorted(Comparator.comparing(Quest::getId)).toList();
            } else {
                quests = pool.getQuests().stream()
                        .filter(q -> !q.isCompleted())
                        .filter(q -> q.isUnlocked() || q.getDefinition().getRequirements().isAlwaysShowInMenu())
                        .sorted(Comparator.comparing(Quest::getId)).toList();
            }
        } else {
            quests = pool.getActiveQuests().stream().sorted(Comparator.comparing(a -> gc.getSortOderMap().get(a.getDefinition().getDifficulty()))).toList();
        }
        return quests;
    }

    private List<Quest> getPage(int page, int pageSize) {
        Collection<Quest> quests = getQuests();
        return quests.stream().skip((long) page * pageSize).limit(pageSize).toList();
    }

    private int getTotalPageCount(int pageSize) {
        var quests = getQuests();
        return (int) Math.ceil((double) quests.size() / pageSize) - 1;
    }
}
