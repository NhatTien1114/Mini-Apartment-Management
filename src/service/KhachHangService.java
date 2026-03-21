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

}
