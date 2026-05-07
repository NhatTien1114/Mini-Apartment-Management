package service;

import dao.ChiSoDienNuocDAO;
import dao.HoaDonDAO;
import dao.HopDongKhachHangDAO;
import dao.QuanLyPhongDAO;
import entity.ChiSoDienNuoc;
import entity.KhachHang;
import entity.Phong;
import entity.RoomMonthSummary;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChiSoDienNuocService {
    private final ChiSoDienNuocDAO chiSoDAO = new ChiSoDienNuocDAO();
    private final QuanLyPhongDAO phongDAO = new QuanLyPhongDAO();
    private final HopDongKhachHangDAO hopDongKhachHangDAO = new HopDongKhachHangDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    public ArrayList<Phong> layTatCaPhong() {
        return phongDAO.getAllPhong();
    }

    public ArrayList<ChiSoDienNuoc> layTatCaTheoThang(int thang, int nam) {
        return chiSoDAO.getAllChiSoThang(thang, nam);
    }

    public List<RoomMonthSummary> layTomTatHoaDonThang(int thang, int nam) {
        return hoaDonDAO.getRoomSummariesTheoThang(thang, nam);
    }

    public String getMaHopDongTaiThangExact(String maPhong, int thang, int nam) {
        return hopDongKhachHangDAO.getMaHopDongTaiThangExact(maPhong, thang, nam);
    }

    public KhachHang getNguoiDaiDienTaiThang(String maPhong, int thang, int nam) {
        return hopDongKhachHangDAO.getNguoiDaiDienByMaPhongTaiThang(maPhong, thang, nam);
    }

    public KhachHang getNguoiDaiDienHienTai(String maPhong) {
        return hopDongKhachHangDAO.getNguoiDaiDienByMaPhong(maPhong);
    }

    public KhachHang getNguoiDaiDienTaiNgay(String maPhong, LocalDate ngayGhi) {
        return hopDongKhachHangDAO.getNguoiDaiDienByMaPhongTaiNgay(maPhong, ngayGhi);
    }

    public int[] layChiSoGanNhatTheoPhongTruocNgay(String maPhong, LocalDate ngayGhi) {
        return chiSoDAO.layChiSoGanNhatTheoPhongTruocNgay(maPhong, ngayGhi);
    }

    public int[] layChiSoGanNhatTheoPhong(String maPhong) {
        return chiSoDAO.layChiSoGanNhatTheoPhong(maPhong);
    }

    /**
     * Lấy chỉ số cũ với fallback: theo hợp đồng → theo phòng tại ngày → theo phòng gần nhất
     */
    public int[] layChiSoCu(String maHopDong, String maPhong, LocalDate ngayGhi) {
        int[] chiSoCu = chiSoDAO.layChiSoTruocNgay(maHopDong, ngayGhi);
        if (chiSoCu[0] == 0 && chiSoCu[1] == 0) {
            chiSoCu = chiSoDAO.layChiSoGanNhatTheoPhongTruocNgay(maPhong, ngayGhi);
            if (chiSoCu[0] == 0 && chiSoCu[1] == 0) {
                chiSoCu = chiSoDAO.layChiSoGanNhatTheoPhong(maPhong);
            }
        }
        return chiSoCu;
    }

    public int[] layChiSoTruocNgay(String maHopDong, LocalDate ngayGhi) {
        return chiSoDAO.layChiSoTruocNgay(maHopDong, ngayGhi);
    }

    public Map<String, String> getMaHopDongTaiThangBatch(int thang, int nam) {
        return hopDongKhachHangDAO.getMaHopDongTaiThangBatch(thang, nam);
    }

    public Map<String, KhachHang> getNguoiDaiDienTaiThangBatch(int thang, int nam) {
        return hopDongKhachHangDAO.getNguoiDaiDienTaiThangBatch(thang, nam);
    }

    public String luu(ChiSoDienNuoc cs) {
        return chiSoDAO.luuHoacCapNhat(cs);
    }

    /**
     * Nếu tháng trước chưa có chỉ số mà đã có hóa đơn, tự động tính và lưu chỉ số từ hóa đơn đó.
     */
    public void autoImportPrevMonthIfMissing(int thang, int nam) {
        int thangTruoc = (thang == 1) ? 12 : thang - 1;
        int namTruoc = (thang == 1) ? nam - 1 : nam;

        ArrayList<ChiSoDienNuoc> existing = chiSoDAO.getAllChiSoThang(thangTruoc, namTruoc);
        Set<String> savedContracts = new HashSet<>();
        for (ChiSoDienNuoc cs : existing)
            savedContracts.add(cs.getMaHopDong());

        List<RoomMonthSummary> summaries = hoaDonDAO.getRoomSummariesTheoThang(thangTruoc, namTruoc);
        for (RoomMonthSummary s : summaries) {
            String maHopDong = hopDongKhachHangDAO.getMaHopDongHienTai(s.maPhong);
            if (maHopDong == null || savedContracts.contains(maHopDong))
                continue;
            LocalDate ngayGhi = LocalDate.of(namTruoc, thangTruoc, 1);
            int[] chiSoCu = chiSoDAO.layChiSoTruocNgay(maHopDong, ngayGhi);
            int soDienMoi = chiSoCu[0] + s.tieuThuDien;
            int soNuocMoi = chiSoCu[1] + s.tieuThuNuoc;
            chiSoDAO.luuHoacCapNhat(new ChiSoDienNuoc(maHopDong, ngayGhi, soDienMoi, soNuocMoi));
        }
    }
}
