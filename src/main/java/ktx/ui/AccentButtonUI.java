package ktx.ui;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

final class AccentButtonUI extends BasicButtonUI {
    @Override
    public void installUI(JComponent component) {
        super.installUI(component);
        AbstractButton button = (AbstractButton) component;
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(KtxTheme.TEXT);
        button.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    @Override
    public void paint(Graphics graphics, JComponent component) {
        AbstractButton button = (AbstractButton) component;
        Graphics2D copy = (Graphics2D) graphics.create();
        copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (!button.isEnabled()) {
            copy.setColor(KtxTheme.BORDER);
        } else if (button.getModel().isPressed()) {
            copy.setColor(KtxTheme.BLUE.darker());
        } else if (button.getModel().isRollover()) {
            copy.setColor(KtxTheme.BLUE_HOVER);
        } else {
            copy.setColor(KtxTheme.BLUE);
        }
        copy.fillRoundRect(0, 0, component.getWidth(), component.getHeight(), 12, 12);
        copy.dispose();
        super.paint(graphics, component);
    }
}
