package ktx.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    static {
        KtxTheme.install();
    }

    public MainFrame() {
        setTitle("Quan ly Ky tuc xa - KTX Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 640));
        setSize(1180, 760);
        setLocationRelativeTo(null);

        AnimatedTabbedPane tabs = new AnimatedTabbedPane();
        tabs.addTab("Quan ly sinh vien", new SinhVienPanel());
        tabs.addTab("Phong va thue phong", new PhongPanel());
        tabs.addTab("Quan ly dich vu", new DichVuPanel());

        addOptionalTab(tabs, "Xe ve thang", "ktx.ui.XeThangPanel");
        addOptionalTab(tabs, "Khach tham", "ktx.ui.KhachThamPanel");
        addOptionalTab(tabs, "Bao hong", "ktx.ui.BaoHongPanel");
        addOptionalTab(tabs, "Hop dong luu tru", "ktx.ui.LuuTruPanel");

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(KtxTheme.BACKGROUND);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 7));
        footer.setBackground(KtxTheme.BACKGROUND);
        footer.setBorder(new EmptyBorder(0, 12, 4, 12));
        JLabel status = new JLabel("KTX MANAGEMENT  •  HỆ THỐNG QUẢN LÝ KÝ TÚC XÁ");
        status.setForeground(KtxTheme.MUTED);
        status.setFont(status.getFont().deriveFont(Font.BOLD, 10f));
        footer.add(status);
        root.add(footer, BorderLayout.SOUTH);

        KtxTheme.style(root);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D copy = (Graphics2D) graphics.create();
                copy.setPaint(new GradientPaint(0, 0, new Color(0x101D33),
                        getWidth(), getHeight(), new Color(0x172C4A)));
                copy.fillRect(0, 0, getWidth(), getHeight());
                copy.setColor(KtxTheme.BLUE);
                copy.fillRect(0, getHeight() - 3, getWidth(), 3);
                copy.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(19, 26, 19, 26));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("KTX Management");
        title.setForeground(KtxTheme.TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 23f));
        JLabel subtitle = new JLabel("Quản lý ký túc xá  •  Sinh viên, phòng ở và dịch vụ");
        subtitle.setForeground(KtxTheme.MUTED);
        subtitle.setFont(subtitle.getFont().deriveFont(12f));
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        JLabel badge = new JLabel("  ●  HỆ THỐNG  ");
        badge.setForeground(new Color(0x93C5FD));
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 11f));
        badge.setBorder(new EmptyBorder(8, 10, 8, 10));
        header.add(titles, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    private void addOptionalTab(JTabbedPane tabs, String title, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (JPanel.class.isAssignableFrom(clazz)) {
                Object panel = clazz.getDeclaredConstructor().newInstance();
                tabs.addTab(title, (JPanel) panel);
            }
        } catch (Exception ignored) {
            // bỏ qua nếu class chưa có
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
