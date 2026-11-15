package ktx.ui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

final class KtxTheme {
    static final Color BACKGROUND = new Color(0x0B1220);
    static final Color SURFACE = new Color(0x111C2E);
    static final Color ELEVATED = new Color(0x17263D);
    static final Color BLUE = new Color(0x2563EB);
    static final Color BLUE_HOVER = new Color(0x3B82F6);
    static final Color TEXT = new Color(0xF8FAFC);
    static final Color MUTED = new Color(0xA8B6CA);
    static final Color BORDER = new Color(0x2A3B54);

    private KtxTheme() {
    }

    static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể khởi tạo giao diện ứng dụng.", exception);
        }

        Font baseFont = new Font("Segoe UI", Font.PLAIN, 13);
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", baseFont);
        UIManager.put("Button.font", baseFont.deriveFont(Font.BOLD));
        UIManager.put("TextField.font", baseFont);
        UIManager.put("TextField.background", ELEVATED);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("TextArea.background", ELEVATED);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("TextArea.caretForeground", TEXT);
        UIManager.put("ComboBox.background", ELEVATED);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", new Color(0x1D4ED8));
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.background", ELEVATED);
        UIManager.put("TableHeader.foreground", TEXT);
        UIManager.put("TabbedPane.background", BACKGROUND);
        UIManager.put("TabbedPane.foreground", MUTED);
        UIManager.put("TabbedPane.selected", SURFACE);
        UIManager.put("TabbedPane.contentAreaColor", SURFACE);
        UIManager.put("TabbedPane.borderColor", BORDER);
        UIManager.put("TabbedPane.focus", BLUE);
        UIManager.put("TabbedPane.font", baseFont.deriveFont(Font.BOLD));
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
    }

    static void style(Component component) {
        if (component instanceof JPanel panel) {
            panel.setBackground(SURFACE);
            panel.setForeground(TEXT);
        } else if (component instanceof JButton button) {
            button.setUI(new AccentButtonUI());
        } else if (component instanceof JRadioButton radioButton) {
            radioButton.setForeground(hintColorFor(radioButton.getBackground()));
        } else if (component instanceof JTextField field) {
            field.setBackground(ELEVATED);
            field.setForeground(TEXT);
            field.setCaretColor(TEXT);
            field.setSelectionColor(BLUE);
            field.setBorder(fieldBorder());
        } else if (component instanceof JTextArea area) {
            area.setBackground(ELEVATED);
            area.setForeground(TEXT);
            area.setCaretColor(TEXT);
            area.setBorder(fieldBorder());
        } else if (component instanceof JComboBox<?> comboBox) {
            comboBox.setBackground(ELEVATED);
            comboBox.setForeground(TEXT);
        } else if (component instanceof JTable table) {
            styleTable(table);
        } else if (component instanceof JScrollPane scrollPane) {
            scrollPane.setBackground(SURFACE);
            scrollPane.getViewport().setBackground(SURFACE);
            scrollPane.setBorder(new LineBorder(BORDER));
        } else if (component instanceof JTabbedPane tabs) {
            tabs.setBackground(BACKGROUND);
            tabs.setForeground(TEXT);
            tabs.setBorder(new EmptyBorder(4, 8, 8, 8));
        } else if (component instanceof JComponent swingComponent) {
            swingComponent.setForeground(TEXT);
        }

        if (component instanceof JComponent swingComponent
                && swingComponent.getBorder() instanceof TitledBorder titledBorder) {
            titledBorder.setTitleColor(BLUE_HOVER);
            titledBorder.setBorder(new LineBorder(BORDER));
        }
        if (component instanceof java.awt.Container container) {
            for (Component child : container.getComponents()) {
                style(child);
            }
        }
    }

    static Color hintColorFor(Color background) {
        double luminance = (0.2126 * background.getRed()
                + 0.7152 * background.getGreen()
                + 0.0722 * background.getBlue()) / 255;
        return luminance >= 0.58 ? new Color(0x64748B) : MUTED;
    }

    private static Border fieldBorder() {
        return new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(7, 9, 7, 9));
    }

    private static void styleTable(JTable table) {
        table.setBackground(SURFACE);
        table.setForeground(TEXT);
        table.setSelectionBackground(new Color(0x1D4ED8));
        table.setSelectionForeground(TEXT);
        table.setGridColor(BORDER);
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value, boolean selected,
                                                           boolean focused, int row, int column) {
                Component cell = super.getTableCellRendererComponent(source, value, selected, focused, row, column);
                cell.setForeground(TEXT);
                cell.setBackground(selected ? source.getSelectionBackground()
                        : row % 2 == 0 ? SURFACE : ELEVATED);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return cell;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);

        JTableHeader header = table.getTableHeader();
        header.setBackground(ELEVATED);
        header.setForeground(TEXT);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        header.setBorder(new LineBorder(BORDER));
        header.setReorderingAllowed(false);
    }
}
