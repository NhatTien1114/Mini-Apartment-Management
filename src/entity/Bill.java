package entity;

import java.util.ArrayList;
import java.util.List;

public class Bill {
    public String phong;
    public String maHopDong;
    public boolean daThanhToan;
    public int tongTieuThuD;
    public double donGiaDien;
    public double tienDien;
    public String maDichVuDien;
    public int tongTieuThuN;
    public double donGiaNuoc;
    public double tienNuoc;
    public String maDichVuNuoc;
    public double tienPhong;
    public String month;
    public String year;
    public List<BillServiceItem> dichVuKhac = new ArrayList<>();
}
