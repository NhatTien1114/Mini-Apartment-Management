package service;

import dao.KhachHangDAO;
import entity.KhachHang;
import java.util.List;

public class KhachHangService {

	private final KhachHangDAO khachHangDAO = new KhachHangDAO();

	public List<KhachHang> layDanhSachKhachHang() {
		return khachHangDAO.layDanhSachKhachHang();
	}

	public KhachHang themKhachHang(KhachHang khachHang) {
		if (khachHang.getHoTen() == null || khachHang.getHoTen().trim().isEmpty()) {
			throw new IllegalArgumentException("Họ tên không được trống.");
		}
		return khachHangDAO.themKhachHang(khachHang);
	}

	public KhachHang themKhachHangVaoPhongDaThue(KhachHang khachHang, String maPhong) {
		if (khachHang.getHoTen() == null || khachHang.getHoTen().trim().isEmpty()) {
			throw new IllegalArgumentException("Họ tên không được trống.");
		}
		if (maPhong == null || maPhong.trim().isEmpty()) {
			throw new IllegalArgumentException("Phòng không hợp lệ.");
		}
		return khachHangDAO.themKhachHangVaoPhongDaThue(khachHang, maPhong.trim());
	}

	public boolean capNhatKhachHang(KhachHang khachHang) {
		if (khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().trim().isEmpty()) {
			throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
		}
		if (khachHang.getHoTen() == null || khachHang.getHoTen().trim().isEmpty()) {
			throw new IllegalArgumentException("Họ tên không được trống.");
		}
		return khachHangDAO.capNhatKhachHang(khachHang);
	}

	public boolean xoaKhachHang(String maKhachHang) {
		if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
			throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
		}
		return khachHangDAO.xoaKhachHang(maKhachHang);
	}

	public KhachHang timTheoMa(String maKhachHang) {
		return khachHangDAO.timTheoMa(maKhachHang);
	}

	public String layMaPhongHienTaiTheoKhach(String maKhachHang) {
		if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
			return "";
		}
		return khachHangDAO.layMaPhongHienTaiTheoKhach(maKhachHang.trim());
	}

	public String layMaPhongCuoiCungTheoKhach(String maKhachHang) {
		if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
			return "";
		}
		return khachHangDAO.layMaPhongCuoiCungTheoKhach(maKhachHang.trim());
	}

	public boolean kiemTraDaRoiDi(String maKhachHang) {
		if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
			return false;
		}
		return khachHangDAO.kiemTraDaRoiDi(maKhachHang.trim());
	}

	public boolean kiemTraCCCDTonTai(String soCCCD, String maKhachHangHienTai) {
		if (soCCCD == null || soCCCD.trim().isEmpty()) {
			return false;
		}
		return khachHangDAO.kiemTraCCCDTonTai(soCCCD.trim(), maKhachHangHienTai);
	}

}
