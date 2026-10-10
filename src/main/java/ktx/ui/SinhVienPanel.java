package ktx.ui;

import ktx.model.SinhVien;
import ktx.service.SinhVienService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class SinhVienPanel extends JPanel {
    private final SinhVienService service=new SinhVienService();
    private final DefaultTableModel model=new DefaultTableModel(new Object[]{"MaSV","SoCMT/CCCD","Ngay sinh","Lop","Que quan"},0){
        public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable table=new JTable(model);
    private final JTextField search=new JTextField(18);
    public SinhVienPanel(){
        setLayout(new BorderLayout(8,8));
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton find=new JButton("Tim kiem"), refresh=new JButton("Tai lai"), add=new JButton("Them"), edit=new JButton("Sua"), del=new JButton("Xoa");
        bar.add(new JLabel("Tu khoa:"));bar.add(search);bar.add(find);bar.add(refresh);bar.add(add);bar.add(edit);bar.add(del);
        add(bar,BorderLayout.NORTH);add(new JScrollPane(table),BorderLayout.CENTER);
        find.addActionListener(e->run(()->fill(service.search(search.getText()))));
        refresh.addActionListener(e->load());
        add.addActionListener(e->editStudent(null));
        edit.addActionListener(e->{int r=table.getSelectedRow();if(r<0){error("Hay chon sinh vien.");return;} editStudent((String)model.getValueAt(r,0));});
        del.addActionListener(e->deleteSelected());
        load();
    }
    private void load(){run(()->fill(service.getAll()));}
    private void fill(List<SinhVien> list){model.setRowCount(0);for(SinhVien s:list)model.addRow(new Object[]{s.maSV(),s.soCMT(),s.ngaySinh(),s.lop(),s.queQuan()});}
    private void editStudent(String id){
        String ma=id==null?ask("Ma sinh vien:"):id;if(ma==null||ma.isBlank())return;
        String cmt=ask("So CMT/CCCD:");if(cmt==null)return;
        String dob=ask("Ngay sinh (yyyy-MM-dd):");if(dob==null)return;
        String lop=ask("Lop:");if(lop==null)return;
        String que=ask("Que quan:");if(que==null)return;
        try{SinhVien s=new SinhVien(ma.trim(),cmt.trim(),LocalDate.parse(dob.trim()),lop.trim(),que.trim());
            if(id==null)service.add(s);else service.update(s);load();
        }catch(Exception ex){error("Khong luu duoc: "+ex.getMessage());}
    }
    private void deleteSelected(){int r=table.getSelectedRow();if(r<0){error("Hay chon sinh vien.");return;}String id=(String)model.getValueAt(r,0);
        if(JOptionPane.showConfirmDialog(this,"Xoa sinh vien "+id+"?","Xac nhan",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
            run(()->service.delete(id));
            load();
        }
    }
    private String ask(String label){return JOptionPane.showInputDialog(this,label);}
    private void run(SqlAction a){try{a.run();}catch(Exception e){error(e.getMessage());}}
    private void error(String s){JOptionPane.showMessageDialog(this,s,"Thong bao",JOptionPane.ERROR_MESSAGE);}
    @FunctionalInterface private interface SqlAction{void run() throws Exception;}
}
