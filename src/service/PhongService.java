package service;

import dao.QuanLyPhongDAO;
import java.util.List;

public class PhongService {
	private final QuanLyPhongDAO phongDAO = new QuanLyPhongDAO();

	public List<QuanLyPhongDAO.Phong> layTatCaPhong() {
		return phongDAO.layTatCa();
	}

	public QuanLyPhongDAO.Phong layTheoMa(String maPhong) {
		if (maPhong == null || maPhong.trim().isEmpty()) {
			throw new IllegalArgumentException("Ma phong khong hop le.");
		}
		return phongDAO.layTheoMa(maPhong);
	}

	public List<QuanLyPhongDAO.Phong> layTheoTang(String maTang) {
		if (maTang == null || maTang.trim().isEmpty()) {
			throw new IllegalArgumentException("Ma tang khong hop le.");
		}
		return phongDAO.layTheoTang(maTang.trim());
	}

	public List<QuanLyPhongDAO.Phong> layTheoToa(String maToa) {
		if (maToa == null || maToa.trim().isEmpty()) {
			throw new IllegalArgumentException("Ma toa khong hop le.");
		}
		return phongDAO.layTheoToa(maToa.trim());
	}

	public String themPhong(String maPhong, long giaThue, String trangThai, List<String> dichVu) {
		return phongDAO.them(maPhong, giaThue, trangThai, dichVu);
	}

	public String capNhatPhong(String maPhong, long giaThue, String trangThai, List<String> dichVu) {
		return phongDAO.capNhat(maPhong, giaThue, trangThai, dichVu);
	}

	public String xoaPhong(String maPhong) {
		if (maPhong == null || maPhong.trim().isEmpty()) {
			throw new IllegalArgumentException("Ma phong khong hop le.");
		}
		return phongDAO.xoa(maPhong.trim());
	}

	public boolean tonTaiPhong(String maPhong) {
		if (maPhong == null || maPhong.trim().isEmpty()) {
			return false;
		}
		return phongDAO.tonTai(maPhong.trim());
	}
}
