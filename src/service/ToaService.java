package service;

import dao.ToaDAO;
import entity.Toa;
import java.util.List;

public class ToaService {

	private final ToaDAO toaDAO = new ToaDAO();

	public List<Toa> layDanhSachToa() {
		return toaDAO.layDanhSachToa();
	}

	public Toa timTheoMa(String maToa) {
		if (maToa == null || maToa.trim().isEmpty()) {
			throw new IllegalArgumentException("Ma toa khong hop le.");
		}
		return toaDAO.timTheoMa(maToa.trim());
	}

	public Toa themToa(Toa toa) {
		validateToa(toa);
		if (toaDAO.tonTai(toa.getMaToa())) {
			throw new IllegalArgumentException("Toa da ton tai: " + toa.getMaToa());
		}
		return toaDAO.themToa(toa);
	}

	public boolean capNhatToa(Toa toa) {
		validateToa(toa);
		return toaDAO.capNhatToa(toa);
	}

	public boolean xoaToa(String maToa) {
		if (maToa == null || maToa.trim().isEmpty()) {
			throw new IllegalArgumentException("Ma toa khong hop le.");
		}
		return toaDAO.xoaToa(maToa.trim());
	}

	private void validateToa(Toa toa) {
		if (toa == null) {
			throw new IllegalArgumentException("Thong tin toa khong duoc rong.");
		}
		if (toa.getMaToa() == null || toa.getMaToa().trim().isEmpty()) {
			throw new IllegalArgumentException("Ma toa khong duoc trong.");
		}
		if (toa.getTenToa() == null || toa.getTenToa().trim().isEmpty()) {
			throw new IllegalArgumentException("Ten toa khong duoc trong.");
		}
		if (toa.getChuHo() == null || toa.getChuHo().getMaChu() == null || toa.getChuHo().getMaChu().trim().isEmpty()) {
			throw new IllegalArgumentException("Chu so huu cua toa khong hop le.");
		}
	}
}
