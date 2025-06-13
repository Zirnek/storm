package net.storm.plugins.core;

import java.awt.Frame;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JFrame;

import com.google.inject.Inject;

import net.runelite.api.ChatMessageType;

public abstract class XPlugin extends net.storm.api.plugins.Plugin {
	
	// Track the current stage in the task execution structure. (for debugging only)
	private double stage = 0.0;
	
	@Inject
	protected LhConfig config;
    
    @Inject
    protected net.runelite.api.Client rlClient;
    
    @Inject
    protected net.storm.sdk.game.Client client;
    
    @Inject
    private net.runelite.client.callback.ClientThread clientThread;
	
    /**
     * Updates & logs the current task stage, <b>IF</b> the <code>condition</code> is met.
     * @param d - current stage
     * @return Returns the same <b>bool</b> as parameter.
     * */
	protected boolean atStage(double d, boolean condition) {
		
		// Track debugging output (only if the debug config is enabled)
		if (condition) {
			stage = d;
			notify("Stage: " + stage, true);
		} else log("Skipped: " + d);
			
		return condition;
	}
	
    /**
     * Writes text to the Client chatBox.
     * @param text - String to output.
     */
    protected void notify(String text) {
    	
    	notify(text, false);
    	
    }
    
    /**
     * Outputs text in the {@link ChatMessageType#GAMEMESSAGE} chat filter.
     * @param text - String to output.
     * @param debug - Whether this should be displayed only if the debug mode is enabled.
     */
    protected void notify(String text, boolean debug) {
    	
    	if (config.debugEnabled() && debug || !debug) {
    	
	    	clientThread.invoke(() -> {
		    	rlClient.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
		    	    	// [23:59:59] prefix
		    			getFormattedTime() + "<col=ff0000>" + text + "</col>", null);
		    	log(text);
	    	});
    	}
    }
    
    /**
     * System out made more convenient to use.
     * @param obj - The variable to output.
     */
    protected void log(Object obj) {
    	System.out.println(getFormattedTime() + obj);
    }
    
    /**
     * 
     * @return A String. Formatted current time: "[HH:mm:ss] "
     */
    protected String getFormattedTime() {
    	final LocalDateTime time = LocalDateTime.now();
    	final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    	
    	return "[" + time.format(timeFormatter) + "] ";
    }
    
    /**
     * 
     * @return Runelite ancestor window component.
     */
    protected final JFrame getRuneliteFrame() {
    	JFrame frame = null;
    	
    	for (Frame f : Frame.getFrames()) {
    	    if (f instanceof JFrame && f.isVisible())
    	        frame = (JFrame) f;
    	};
    	
    	return frame;
    }
	
}
