package net.storm.plugins;

import java.awt.Dimension;
import java.awt.Graphics2D;

import net.runelite.client.ui.overlay.components.PanelComponent;
import net.storm.sdk.script.paint.DefaultPaint;

public class MainOverlay extends DefaultPaint {
	private final PanelComponent panelComponent = new PanelComponent();
	
	@Override
	public Dimension render(Graphics2D graphics) {
		
		return panelComponent.render(graphics);
	}

}
