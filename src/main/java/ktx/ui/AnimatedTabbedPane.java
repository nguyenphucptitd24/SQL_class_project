package ktx.ui;

import javax.swing.JTabbedPane;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

final class AnimatedTabbedPane extends JTabbedPane {
    private static final int TRANSITION_DURATION_MS = 180;
    private BufferedImage previousView;
    private Rectangle previousBounds;
    private long transitionStartedAt;
    private Timer transitionTimer;

    @Override
    public void setSelectedIndex(int index) {
        int previousIndex = getSelectedIndex();
        BufferedImage snapshot = null;
        Rectangle bounds = null;

        if (previousIndex >= 0 && index != previousIndex && isShowing()) {
            Component previous = getSelectedComponent();
            if (previous != null && previous.getWidth() > 0 && previous.getHeight() > 0) {
                snapshot = new BufferedImage(previous.getWidth(), previous.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = snapshot.createGraphics();
                previous.printAll(graphics);
                graphics.dispose();
                bounds = previous.getBounds();
            }
        }

        super.setSelectedIndex(index);
        previousView = snapshot;
        previousBounds = bounds;
        transitionStartedAt = System.currentTimeMillis();

        if (transitionTimer != null) {
            transitionTimer.stop();
        }
        if (previousView != null) {
            transitionTimer = new Timer(16, event -> {
                if (System.currentTimeMillis() - transitionStartedAt >= TRANSITION_DURATION_MS) {
                    previousView = null;
                    transitionTimer.stop();
                }
                repaint();
            });
            transitionTimer.start();
        }
    }

    @Override
    protected void paintChildren(Graphics graphics) {
        super.paintChildren(graphics);
        if (previousView == null || previousBounds == null) {
            return;
        }

        float progress = Math.min(1f,
                (System.currentTimeMillis() - transitionStartedAt) / (float) TRANSITION_DURATION_MS);
        Graphics2D copy = (Graphics2D) graphics.create();
        copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - progress));
        copy.drawImage(previousView, previousBounds.x, previousBounds.y, null);
        copy.dispose();
    }
}
