package net.storm.plugins.components;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.FocusManager;
import javax.swing.JTextField;

public class PromptTextField extends JTextField {
    
    private static final long serialVersionUID = 1L;
    private String string;
    
    public PromptTextField(String prompt) {
        this.string = prompt;
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
        
            if(getText().isEmpty() && ! (FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)){
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setColor(Color.GRAY);
                    g2.drawString(string, 6, g2.getFontMetrics().getHeight() + 2);  // Figure out: x, y from the FontMetrics and size of the component.
                    g2.dispose();
            }
      }
}
