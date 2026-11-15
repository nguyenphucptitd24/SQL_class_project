package ktx.ui;

import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

final class HintTextField extends JTextField {
    private final String hint;

    HintTextField(String hint, int columns) {
        super(columns);
        this.hint = hint;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (!hint.isEmpty() && getText().isEmpty() && !hasFocus() && isEnabled()) {
            Graphics2D copy = (Graphics2D) graphics.create();
            Insets insets = getInsets();
            copy.setColor(KtxTheme.hintColorFor(getBackground()));
            int baseline = (getHeight() - copy.getFontMetrics().getHeight()) / 2
                    + copy.getFontMetrics().getAscent();
            copy.drawString(hint, insets.left + 1, baseline);
            copy.dispose();
        }
    }
}
