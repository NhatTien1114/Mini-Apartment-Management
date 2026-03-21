package dao;

import database.connectDB;
import entity.KhachHang;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

	private static final String MA_PREFIX = "KH";

	public String taoMaKhachHangMoi() {
		String sql = "SELECT MAX(CAST(SUBSTRING(maKhachHang, 3, LEN(maKhachHang) - 2) AS INT)) AS maxSo "
				+ "FROM KhachHang WHERE maKhachHang LIKE 'KH%'";

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
				int soTiepTheo = 1;
				if (rs.next()) {
					int maxSo = rs.getInt("maxSo");
					if (!rs.wasNull()) {
						soTiepTheo = maxSo + 1;
					}
				}

				return MA_PREFIX + String.format("%03d", soTiepTheo);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Khong the tao ma khach hang moi.", e);
		}
	}

	public KhachHang themKhachHang(KhachHang khachHang) {
		String sql = "INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, ngaySinh, soCCCD, diaChiThuongTru) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		if (khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().trim().isEmpty()) {
			khachHang.setMaKhachHang(taoMaKhachHangMoi());
		}

		try {
			Connection con = connectDB.getConnection();
			int soDong;
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, khachHang.getMaKhachHang());
				ps.setString(2, khachHang.getHoTen());
				ps.setString(3, khachHang.getSoDienThoai());

				if (khachHang.getNgaySinh() != null) {
					ps.setDate(4, Date.valueOf(khachHang.getNgaySinh()));
				} else {
					ps.setDate(4, null);
				}

				ps.setString(5, khachHang.getSoCCCD());
				ps.setString(6, khachHang.getDiaChi());

				soDong = ps.executeUpdate();
			}

			if (soDong == 0) {
				throw new RuntimeException("Them khach hang that bai.");
			}

			return khachHang;
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi them khach hang vao database.", e);
		}
	}

	public List<KhachHang> layDanhSachKhachHang() {
		String sql = "SELECT maKhachHang, hoTen, soDienThoai, ngaySinh, soCCCD, diaChiThuongTru FROM KhachHang ORDER BY maKhachHang";
		List<KhachHang> ketQua = new ArrayList<>();

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					KhachHang kh = new KhachHang();
					kh.setMaKhachHang(rs.getString("maKhachHang"));
					kh.setHoTen(rs.getString("hoTen"));
					kh.setSoDienThoai(rs.getString("soDienThoai"));

					Date ngaySinh = rs.getDate("ngaySinh");
					if (ngaySinh != null) {
						kh.setNgaySinh(ngaySinh.toLocalDate());
					}

					kh.setSoCCCD(rs.getString("soCCCD"));
					kh.setDiaChi(rs.getString("diaChiThuongTru"));
					ketQua.add(kh);
				}
			}
			return ketQua;
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi lay danh sach khach hang.", e);
		}
	}

	public boolean capNhatKhachHang(KhachHang khachHang) {
		String sql = "UPDATE KhachHang SET hoTen = ?, soDienThoai = ?, ngaySinh = ?, soCCCD = ?, diaChiThuongTru = ? "
				+ "WHERE maKhachHang = ?";

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, khachHang.getHoTen());
				ps.setString(2, khachHang.getSoDienThoai());

				if (khachHang.getNgaySinh() != null) {
					ps.setDate(3, Date.valueOf(khachHang.getNgaySinh()));
				} else {
					ps.setDate(3, null);
				}

				ps.setString(4, khachHang.getSoCCCD());
				ps.setString(5, khachHang.getDiaChi());
				ps.setString(6, khachHang.getMaKhachHang());

				int soDong = ps.executeUpdate();
				return soDong > 0;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi cap nhat khach hang.", e);
		}
	}

	public boolean xoaKhachHang(String maKhachHang) {
		String sql = "DELETE FROM KhachHang WHERE maKhachHang = ?";

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, maKhachHang);

				int soDong = ps.executeUpdate();
				return soDong > 0;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi xoa khach hang.", e);
		}
	}

	public KhachHang timTheoMa(String maKhachHang) {
		String sql = "SELECT maKhachHang, hoTen, soDienThoai, ngaySinh, soCCCD, diaChiThuongTru FROM KhachHang WHERE maKhachHang = ?";

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, maKhachHang);

				try (ResultSet rs = ps.executeQuery()) {
					KhachHang kh = null;
					if (rs.next()) {
						kh = new KhachHang();
						kh.setMaKhachHang(rs.getString("maKhachHang"));
						kh.setHoTen(rs.getString("hoTen"));
						kh.setSoDienThoai(rs.getString("soDienThoai"));

						Date ngaySinh = rs.getDate("ngaySinh");
						if (ngaySinh != null) {
							kh.setNgaySinh(ngaySinh.toLocalDate());
						}

						kh.setSoCCCD(rs.getString("soCCCD"));
						kh.setDiaChi(rs.getString("diaChiThuongTru"));
					}
					return kh;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi tim khach hang theo ma.", e);
		}
	}

}
