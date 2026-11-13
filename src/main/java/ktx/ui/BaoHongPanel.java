package ktx.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Module Báo hỏng và bảo trì phòng (5 tab: Tạo báo hỏng, Danh sách, Cập nhật xử lý, Chi tiết & lịch sử, Thống kê).
 *
 * Tích hợp vào MainFrame bằng một dòng:
 *     tabs.addTab("Báo hỏng", new BaoHongPanel());
 *
 * Chạy riêng để thử module (không cần sửa MainFrame): chạy hàm main của lớp này.
 */
public class BaoHongPanel extends JPanel {
    public BaoHongPanel() {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        ChiTietBaoHongTab chiTiet = new ChiTietBaoHongTab();
        CapNhatBaoHongTab capNhat = new CapNhatBaoHongTab();
        DanhSachBaoHongTab danhSach = new DanhSachBaoHongTab(
            id -> { chiTiet.hienThi(id); tabs.setSelectedComponent(chiTiet); },
            id -> { capNhat.tai(id); tabs.setSelectedComponent(capNhat); });
        TaoBaoHongTab tao = new TaoBaoHongTab(id -> { });

        tabs.addTab("Tạo báo hỏng", tao);
        tabs.addTab("Danh sách", danhSach);
        tabs.addTab("Cập nhật xử lý", capNhat);
        tabs.addTab("Chi tiết & lịch sử", chiTiet);
        tabs.addTab("Thống kê", new ThongKeBaoHongTab());
        // Dữ liệu có thể đổi ở tab khác (hoặc module khác), nên làm mới tab khi người dùng chuyển sang.
        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof KhachThamUi.TaiLai t) t.taiLai();
        });
        add(tabs, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Báo hỏng và bảo trì phòng - KTX");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1150, 650);
            f.setLocationRelativeTo(null);
            f.setContentPane(new BaoHongPanel());
            f.setVisible(true);
        });
    }
}
