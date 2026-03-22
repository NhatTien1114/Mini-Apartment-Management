package service;

import dao.TangDAO;
import entity.Tang;
import java.util.List;

public class TangService {

	private final TangDAO tangDAO = new TangDAO();

	public List<Tang> layDanhSachTang() {
		return tangDAO.layDanhSachTang();
	}

	public List<Tang> layTheoToa(String maToa) {
		if (maToa == null || maToa.trim().isEmpty()) {
			throw new IllegalArgumentException("Ma toa khong hop le.");
		}
		return tangDAO.layTheoToa(maToa.trim());
	}

	public Tang timTheoMa(String maTang) {
		if (maTang == null || maTang.trim().isEmpty()) {
			throw new IllegalArgumentException("Ma tang khong hop le.");
		}
		return tangDAO.timTheoMa(maTang.trim());
	}

	public Tang themTang(Tang tang) {
		validateTang(tang);
		if (tangDAO.tonTai(tang.getMaTang())) {
			throw new IllegalArgumentException("Tang da ton tai: " + tang.getMaTang());
		}
		return tangDAO.themTang(tang);
	}

	public boolean capNhatTang(Tang tang) {
		validateTang(tang);
		return tangDAO.capNhatTang(tang);
	}

	public boolean xoaTang(String maTang) {
		if (maTang == null || maTang.trim().isEmpty()) {
			throw new IllegalArgumentException("Ma tang khong hop le.");
		}
		return tangDAO.xoaTang(maTang.trim());
	}

	private void validateTang(Tang tang) {
		if (tang == null) {
			throw new IllegalArgumentException("Thong tin tang khong duoc rong.");
		}
		if (tang.getMaTang() == null || tang.getMaTang().trim().isEmpty()) {
			throw new IllegalArgumentException("Ma tang khong duoc trong.");
		}
		if (tang.getTenTang() == null || tang.getTenTang().trim().isEmpty()) {
			throw new IllegalArgumentException("Ten tang khong duoc trong.");
		}
		if (tang.getToa() == null || tang.getToa().getMaToa() == null || tang.getToa().getMaToa().trim().isEmpty()) {
			throw new IllegalArgumentException("Toa cua tang khong hop le.");
		}
	}
}
