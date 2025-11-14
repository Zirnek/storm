package net.storm.plugins;

import org.pf4j.Extension;

import com.google.inject.Inject;
import com.google.inject.Provides;

import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import net.storm.api.plugins.Plugin;
import net.storm.api.plugins.PluginDescriptor;
import net.storm.api.plugins.config.ConfigManager;

/**
 * 
 */
@PluginDescriptor(
		name = "Dream Builder", 
		tags = { "dream", "builder", "ironman", "iron", "hcim", "f2p" })
@Extension
public final class Main extends Plugin {
	
	@Inject
	private MainOverlay mainOverlay;
	
	@Inject
    private OverlayManager overlayManager;

	
	@Override
	public void startUp() throws Exception {
		overlayManager.add(mainOverlay);
	}

	@Override
	public void shutDown() throws Exception {
		overlayManager.remove(mainOverlay);
	}
	
	
	@Subscribe
	public void onGameTick(GameTick game) {
		Utils.log("Running");
	}
	

	@Provides
	MainConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(MainConfig.class);
	}

}
