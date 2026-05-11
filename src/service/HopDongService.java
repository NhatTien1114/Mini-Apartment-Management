package service;

import dao.HopDongDAO;
import dao.KhachHangDAO;
import entity.HopDong;
import java.time.LocalDate;
import java.util.ArrayList;
import ui.main.HopDongUI;

public class HopDongService {
    private final HopDongDAO hopDongDAO = new HopDongDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    public ArrayList<HopDong> getAllHopDong() {
        return hopDongDAO.getAllHopDong();
    }

    public ArrayList<HopDong> getAllHopDongDangHieuLuc() {
        return hopDongDAO.getAllHopDongDangHieuLuc();
    }

    public HopDong getHopDongByMaPhong(String maPhong) {
        return hopDongDAO.getHopDongByMaPhong(maPhong);
    }

    public HopDong getHopDongByMaHopDong(String maHopDong) {
        return hopDongDAO.getHopDongByMaHopDong(maHopDong);
    }

    public boolean luuHopDongMoi(HopDongUI.ContractDraft draft) {
        return hopDongDAO.luuHopDongMoi(draft);
    }

    public boolean capNhatThongTinHopDong(String maHopDong, String ngayBatDau, String ngayKetThuc,
            double tienCoc, double tienThueThang, String maKhachHang,
            String hoTen, String soDienThoai, String cccd, String diaChi) {
        return hopDongDAO.capNhatThongTinHopDong(maHopDong, ngayBatDau, ngayKetThuc,
                tienCoc, tienThueThang, maKhachHang, hoTen, soDienThoai, cccd, diaChi);
    }

    public boolean xoaHopDongVaKhachHangLienQuan(String maHopDong) {
        return hopDongDAO.xoaHopDongVaKhachHangLienQuan(maHopDong);
    }

    public boolean ketThucHopDong(String maHopDong, String maPhong, LocalDate ngayKetThuc) {
        return hopDongDAO.ketThucHopDong(maHopDong, maPhong, ngayKetThuc);
    }

    public int capNhatHopDongHetHanTuDong() {
        return hopDongDAO.capNhatHopDongHetHanTuDong();
    }

    public boolean kiemTraCCCDTonTai(String cccd, String maKhachHang) {
        return khachHangDAO.kiemTraCCCDTonTai(cccd, maKhachHang);
    }

    public String getLastError() {
        return hopDongDAO.getLastError();
    }
}
