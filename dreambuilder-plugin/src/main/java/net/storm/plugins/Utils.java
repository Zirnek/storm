package net.storm.plugins;

import java.awt.Color;

import net.storm.sdk.utils.MessageUtils;

public final class Utils {
	
    /**
     * Writes text to the Client chatBox.
     * @param text - String to output.
     */
    public static void log(String text) {
		System.out.println(text);
    	MessageUtils.addMessage(text, Color.BLUE);
    }
    
    public static int random(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
	
}
