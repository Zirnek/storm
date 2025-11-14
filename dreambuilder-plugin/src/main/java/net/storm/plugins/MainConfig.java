package net.storm.plugins;

import net.runelite.api.Skill;
import net.storm.api.plugins.SoxExclude;
import net.storm.api.plugins.config.Config;
import net.storm.api.plugins.config.ConfigGroup;
import net.storm.api.plugins.config.ConfigItem;
import net.storm.api.plugins.config.ConfigSection;

@ConfigGroup(MainConfig.GROUP)
@SoxExclude
public interface MainConfig extends Config {
    String GROUP = "group_id";
    
    @ConfigItem(
            description = "",
            keyName = "tasksQueue",
            name = "Tasks Queue",
            position = 0
    )
    default String itemNames() {
        return "";
    }
    
    @ConfigSection(
            description = "",
            name = "XP Lamp Settings",
            position = 1
    )
    String xpLampSection = "xpLampSection";
    
    @ConfigItem(
    	    description = "",
    	    keyName = "xpRewardArdougneEasy",
    	    name = "Ardougne Easy",
            section = xpLampSection,
            position = 2
    )
    default Skill xpRewardArdougneEasy() {
    	return Skill.PRAYER;
    }
}
