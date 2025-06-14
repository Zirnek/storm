package net.storm.plugins.core;

import net.storm.api.plugins.SoxExclude;
import net.storm.api.plugins.config.Button;
import net.storm.api.plugins.config.Config;
import net.storm.api.plugins.config.ConfigGroup;
import net.storm.api.plugins.config.ConfigItem;
import net.storm.api.plugins.config.ConfigSection;

@ConfigGroup(LhConfig.GROUP)
@SoxExclude
public interface LhConfig extends Config {
    String GROUP = "loot_handler";
    
    @ConfigItem(
            description = "Enable/Disable the plugin", 
            keyName = "scriptEnabled",
            name = "Enabled",
            position = 0
    )
    default boolean scriptEnabled() {
        return false;
    }
    
    @ConfigItem(
            description = "Use `,` symbol as a splitter<br>Append `=` symbol to indicate the amount of items to retain<br>(optional, e.g. Amethyst arrow=300)",
            keyName = "itemNames",
            name = "Items to sell",
            position = 1
    )
    default String itemNames() {
        return "";
    }

    @ConfigSection(
            description = "",
            name = "Utils",
            position = 2
    )
    String utilsSection = "utilsSection";

    @ConfigItem(
            description = "Name of the NPC whose drops to fetch",
            keyName = "npcName",
            name = "NPC Name",
            section = utilsSection,
            position = 3
    )
    default String npcName() {
        return "";
    }
    
    @ConfigItem(
            description = "",
            keyName = "fetchButton",
            name = "Fetch Drops",
            section = utilsSection,
            position = 4
    )
    default Button fetchButton() {
        return new Button();
    }
    
    @ConfigItem(
            description = "",
            keyName = "npcDrops",
            name = "",
            section = utilsSection,
            position = 5
    )
    default String npcDrops() {
        return "";
    }
    
    @ConfigSection(
            description = "",
            name = "Presets",
            position = 6
    )
    String presetsSection = "presetsSection";
    
    @ConfigItem(
            description = "",
            keyName = "manageButton",
            name = "Manage Presets",
            section = presetsSection,
            position = 7
    )
    default Button manageButton() {
        return new Button();
    }
    
    @ConfigItem(
            description = "For debugging purposes only.<br>Leave this disabled, unless you've encountered a bug.", 
            keyName = "debugEnabled",
            name = "Debug",
            position = 8
    )
    default boolean debugEnabled() {
        return false;
    }
}
