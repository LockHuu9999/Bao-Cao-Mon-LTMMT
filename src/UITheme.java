import javax.swing.*;
import java.awt.*;

/**
 * Small UI theme helper to apply a consistent look across forms.
 * Call UITheme.apply(this) from a JFrame after initComponents().
 */
public class UITheme {
    public static void apply(JFrame frame) {
        // try to set Nimbus L&F if available (non-fatal)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            // ignore, use default
        }

        // Base font and colours for a modern consistent look
        Font base = new Font("Segoe UI", Font.PLAIN, 14);
        UIManager.put("Label.font", base);
        UIManager.put("Button.font", base);
        UIManager.put("TextField.font", base);
        UIManager.put("ToggleButton.font", base);
        UIManager.put("ComboBox.font", base);
        UIManager.put("Panel.background", new Color(250, 250, 250));
        UIManager.put("Button.background", new Color(230, 230, 230));
        UIManager.put("TextField.background", Color.WHITE);

        // apply to the current window
        SwingUtilities.updateComponentTreeUI(frame);
        // ensure the frame repaints with new settings
        frame.invalidate();
        frame.validate();
        frame.repaint();
    }
}
