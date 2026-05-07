package entity;

import java.util.ArrayList;
import java.util.List;

public class RoomMonthSummary {
    public String maHoaDon = "";
    public String maPhong;
    public double tienPhong;
    public int tieuThuDien;
    public double donGiaDien;
    public int tieuThuNuoc;
    public double donGiaNuoc;
    public boolean daThanhToan;
    public List<BillServiceItem> services = new ArrayList<>();
}
