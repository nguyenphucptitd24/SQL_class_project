package ktx.ui;

import ktx.database.DatabaseConnection;
import ktx.model.Phong;
import ktx.repository.PhongRepository;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class PhongPanel extends JPanel {
    private final PhongRepository repo=new PhongRepository();
    private final DefaultTableModel model=new DefaultTableModel(new Object[]{"So phong","Loai phong","Don gia/thang","Suc chua","Dang o","Con trong"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable table=new JTable(model);
    public PhongPanel(){
        setLayout(new BorderLayout(8,8));JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh=new JButton("Tai lai"),add=new JButton("Them phong"),edit=new JButton("Sua phong"),assign=new JButton("Xep phong"),checkout=new JButton("Tra phong");
        for(JButton b:new JButton[]{refresh,add,edit,assign,checkout})bar.add(b);
        add(bar,BorderLayout.NORTH);add(new JScrollPane(table),BorderLayout.CENTER);
        refresh.addActionListener(e->load());add.addActionListener(e->editRoom(null));edit.addActionListener(e->{int r=table.getSelectedRow();if(r<0){err("Hay chon phong.");return;}editRoom((String)model.getValueAt(r,0));});
        assign.addActionListener(e->assignRoom());checkout.addActionListener(e->checkout());load();
    }
    private void load(){try{List<Phong> list=repo.findAll();model.setRowCount(0);for(Phong p:list)model.addRow(new Object[]{p.soPhong(),p.loaiPhong(),p.donGia(),p.soNguoiToiDa(),p.soNguoiHienTai(),Math.max(0,p.soNguoiToiDa()-p.soNguoiHienTai())});}catch(Exception e){err(e.getMessage());}}
    private void editRoom(String id){try{
        String so=id==null?ask("So phong:"):id;if(so==null||so.isBlank())return;
        String loai=ask("Loai phong:");if(loai==null)return;
        BigDecimal gia=new BigDecimal(ask("Don gia/thang (VND):").trim());
        int max=Integer.parseInt(ask("So nguoi toi da:").trim());if(gia.signum()<0||max<=0)throw new IllegalArgumentException("Don gia >= 0 va suc chua > 0.");
        if(id==null)repo.insert(new Phong(so.trim(),loai.trim(),gia,max,0));else{
            int occupied=0;for(Phong p:repo.findAll())if(p.soPhong().equals(id))occupied=p.soNguoiHienTai();
            if(max<occupied)throw new IllegalArgumentException("Suc chua moi khong duoc nho hon so nguoi dang o.");
            repo.update(new Phong(id,loai.trim(),gia,max,occupied));
        }load();
    }catch(Exception e){err("Khong luu duoc: "+e.getMessage());}}
    private void assignRoom(){String sv=ask("Ma sinh vien:");if(sv==null)return;String room=ask("So phong:");if(room==null)return;String date=ask("Ngay bat dau (yyyy-MM-dd):");if(date==null)return;
        try{new ktx.service.ThuePhongService().xepPhong(sv,room,java.time.LocalDate.parse(date.trim()));JOptionPane.showMessageDialog(this,"Xep phong thanh cong.");load();}catch(Exception e){err(e.getMessage());}}
    private void checkout(){String sv=ask("Ma sinh vien can tra phong:");if(sv==null)return;String date=ask("Ngay tra (yyyy-MM-dd):");if(date==null)return;
        try{new ktx.service.ThuePhongService().traPhong(sv,java.time.LocalDate.parse(date.trim()));JOptionPane.showMessageDialog(this,"Tra phong thanh cong.");load();}catch(Exception e){err(e.getMessage());}}
    private String ask(String s){return JOptionPane.showInputDialog(this,s);}
    private void err(String s){JOptionPane.showMessageDialog(this,s,"Loi",JOptionPane.ERROR_MESSAGE);}
}
