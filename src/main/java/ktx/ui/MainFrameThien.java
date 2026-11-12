
package ktx.ui;

import javax.swing.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Quan ly Ky tuc xa - KTX Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 620);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();

        // Cac chuc nang co san
        tabs.addTab("Quan ly sinh vien", new SinhVienPanel());
        tabs.addTab("Phong va thue phong", new PhongPanel());
        tabs.addTab("Quan ly dich vu", new DichVuPanel());

        // Cac chuc nang mo rong
        addOptionalTab(tabs, "Xe ve thang", "ktx.ui.XeThangPanel");
        addOptionalTab(tabs, "Khach tham", "ktx.ui.KhachThamPanel");

        // Module quan ly hop dong luu tru
        addOptionalTab(
            tabs,
            "Hop dong luu tru",
            "ktx.ui.LuuTruPanel"
        );

        setContentPane(tabs);
    }

    // Them tab neu class giao dien ton tai
    private void addOptionalTab(
            JTabbedPane tabs,
            String title,
            String className
    ) {

        try {
            Class<?> clazz = Class.forName(className);

            if (JPanel.class.isAssignableFrom(clazz)) {

                Object panel = clazz
                    .getDeclaredConstructor()
                    .newInstance();

                tabs.addTab(title, (JPanel) panel);
            }

        } catch (Exception e) {
            System.err.println(
                "Khong the mo tab " + title + ": " + e.getMessage()
            );
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
