package ktx.ui;

import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Quan ly Ky tuc xa - KTX Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 620);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Quan ly sinh vien", new SinhVienPanel());
        tabs.addTab("Phong va thue phong", new PhongPanel());
        tabs.addTab("Quan ly dich vu", new DichVuPanel());

        addOptionalTab(tabs, "Xe ve thang", "ktx.ui.XeThangPanel");
        addOptionalTab(tabs, "Khach tham", "ktx.ui.KhachThamPanel");

        setContentPane(tabs);
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