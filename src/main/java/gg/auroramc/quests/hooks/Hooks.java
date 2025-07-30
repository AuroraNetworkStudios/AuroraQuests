package gg.auroramc.quests.hooks;

import gg.auroramc.quests.hooks.adyeshach.AdyeshachHook;
import gg.auroramc.quests.hooks.auraskills.AuraSkillsHook;
import gg.auroramc.quests.hooks.auroralevels.AuroraLevelsHook;
import gg.auroramc.quests.hooks.citizens.CitizensHook;
import gg.auroramc.quests.hooks.customfishing.CustomFishingHook;
import gg.auroramc.quests.hooks.economyshopgui.EconomyShopGuiHook;
import gg.auroramc.quests.hooks.excellentshop.ExcellentShopHook;
import gg.auroramc.quests.hooks.fancynpcs.FancyNPCsHook;
import gg.auroramc.quests.hooks.luckperms.LuckPermsHook;
import gg.auroramc.quests.hooks.mmolib.MMOLibHook;
import gg.auroramc.quests.hooks.mythicdungeons.DungeonsHook;
import gg.auroramc.quests.hooks.mythicmobs.MythicHook;
import gg.auroramc.quests.hooks.nexo.NexoHook;
import gg.auroramc.quests.hooks.shopguiplus.ShopGUIPlusHook;
import gg.auroramc.quests.hooks.shopkeepers.ShopkeepersHook;
import gg.auroramc.quests.hooks.superiorskyblock.SuperiorSkyblockHook;
import gg.auroramc.quests.hooks.worldguard.WorldGuardHook;
import gg.auroramc.quests.hooks.znpcs.ZnpcsHook;
import gg.auroramc.quests.hooks.znpcsplus.ZnpcPlusHook;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Set;

@Getter
public enum Hooks {
    AURORA_LEVELS(AuroraLevelsHook.class, "AuroraLevels"),
    AURA_SKILLS(AuraSkillsHook.class, "AuraSkills"),
    CUSTOM_FISHING(CustomFishingHook.class, "CustomFishing"),
    MYTHIC_MOBS(MythicHook.class, "MythicMobs"),
    MYTHIC_DUNGEONS(DungeonsHook.class, "MythicDungeons"),
    WORLD_GUARD(WorldGuardHook.class, "WorldGuard"),
    CITIZENS(CitizensHook.class, "Citizens"),
    SHOPKEEPERS(ShopkeepersHook.class, "Shopkeepers"),
    MMOLIB(MMOLibHook.class, "MythicLib"),
    SHOP_GUI_PLUS(ShopGUIPlusHook.class, "ShopGUIPlus"),
    ECONOMY_SHOP_GUI(EconomyShopGuiHook.class, Set.of("EconomyShopGUI", "EconomyShopGUI-Premium")),
    LUCK_PERMS(LuckPermsHook.class, "LuckPerms"),
    ADYESHACH(AdyeshachHook.class, "Adyeshach"),
    SUPERIOR_SKYBLOCK(SuperiorSkyblockHook.class, "SuperiorSkyblock2"),
    FANCY_NPCS(FancyNPCsHook .class, "FancyNpcs"),
    ZNPCS(ZnpcsHook.class, "ServersNPC"),
    EXCELLENT_SHOP(ExcellentShopHook.class, "ExcellentShop"),
    NEXO(NexoHook.class, "Nexo"),
    ZNPCSPlus(ZnpcPlusHook.class, "ZNPCsPlus");

    private final Class<? extends Hook> clazz;
    private final Set<String> plugins;

    Hooks(Class<? extends Hook> clazz, String plugin) {
        this.clazz = clazz;
        this.plugins = Set.of(plugin);
    }

    Hooks(Class<? extends Hook> clazz, Set<String> plugins) {
        this.clazz = clazz;
        this.plugins = plugins;
    }

    public boolean canHook() {
        for (String plugin : plugins) {
            if (Bukkit.getPluginManager().getPlugin(plugin) != null) {
                return true;
            }
        }
        return false;
    }
}
