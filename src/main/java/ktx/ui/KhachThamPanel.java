package ktx.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Module Quản lý khách thăm (gồm 4 tab: Khách, Ghi nhận lượt thăm, Lịch sử, Thống kê).
 *
 * Tích hợp vào MainFrame bằng một dòng:
 *     tabs.addTab("Khách thăm", new KhachThamPanel());
 *
 * Chạy riêng để thử module (không cần sửa MainFrame): chạy hàm main của lớp này.
 */
public class KhachThamPanel extends JPanel {
    public KhachThamPanel() {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Khách", new KhachTab());
        tabs.addTab("Ghi nhận lượt thăm", new GhiNhanTab());
        tabs.addTab("Lịch sử", new LichSuTab());
        tabs.addTab("Thống kê", new ThongKeTab());
        // Dữ liệu có thể đổi ở tab khác (hoặc module khác), nên làm mới tab khi người dùng chuyển sang.
        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof KhachThamUi.TaiLai t) t.taiLai();
        });
        add(tabs, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Quản lý khách thăm - KTX");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1050, 620);
            f.setLocationRelativeTo(null);
            f.setContentPane(new KhachThamPanel());
            f.setVisible(true);
        });
    }
}
