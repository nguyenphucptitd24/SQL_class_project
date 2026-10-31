package ktx.ui;

import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Quan ly Ky tuc xa - KTX Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050,620);setLocationRelativeTo(null);
        JTabbedPane tabs=new JTabbedPane();
        tabs.addTab("Quan ly sinh vien",new SinhVienPanel());
        tabs.addTab("Phong va thue phong",new PhongPanel());
        tabs.addTab("Xe ve thang",new XeThangPanel());
        setContentPane(tabs);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->new MainFrame().setVisible(true));
    }
}
