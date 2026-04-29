package dao;

import database.connectDB;
import entity.KhachHang;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KhachHangDAO {

	private static final String MA_PREFIX = "KH";

	private String taoMaTheoThoiGian(String prefix) {
		long millis = System.currentTimeMillis() % 1_000_000_000_000L;
		int random = (int) (Math.random() * 1000);
		return prefix + String.format("%012d%03d", millis, random);
	}

	private String timHopDongDangHieuLucTheoPhong(Connection con, String maPhong) throws SQLException {
		String sql = "SELECT TOP 1 maHopDong FROM HopDong WHERE maPhong = ? AND trangThai = 1 ORDER BY ngayBatDau DESC";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, maPhong);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getString("maHopDong") : null;
			}
		}
	}

	private void themLienKetHopDongKhachHang(Connection con, String maHopDong, String maKhachHang, int vaiTro)
			throws SQLException {
		String sql = "INSERT INTO HopDongKhachHang (maHDKT, maHopDong, maKhachHang, vaiTro) VALUES (?, ?, ?, ?)";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, taoMaTheoThoiGian("HDKT"));
			ps.setString(2, maHopDong);
			ps.setString(3, maKhachHang);
			ps.setInt(4, vaiTro);
			ps.executeUpdate();
		}
	}

	private void tangSoNguoiPhong(Connection con, String maPhong) throws SQLException {
		String sql = "UPDATE Phong SET soNguoiHienTai = ISNULL(soNguoiHienTai, 0) + 1 WHERE maPhong = ?";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, maPhong);
			if (ps.executeUpdate() == 0) {
				throw new SQLException("Khong tim thay phong de cap nhat so nguoi: " + maPhong);
			}
		}
	}

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

				return MA_PREFIX + String.format("%02d", soTiepTheo);
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

	public KhachHang themKhachHangVaoPhongDaThue(KhachHang khachHang, String maPhong) {
		if (maPhong == null || maPhong.trim().isEmpty()) {
			throw new IllegalArgumentException("Phong khong hop le.");
		}

		Connection con = null;
		try {
			con = connectDB.getConnection();
			con.setAutoCommit(false);

			if (khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().trim().isEmpty()) {
				khachHang.setMaKhachHang(taoMaKhachHangMoi());
			}

			String sqlInsertKH = "INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, ngaySinh, soCCCD, diaChiThuongTru) "
					+ "VALUES (?, ?, ?, ?, ?, ?)";
			try (PreparedStatement ps = con.prepareStatement(sqlInsertKH)) {
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

				if (ps.executeUpdate() == 0) {
					throw new SQLException("Them khach hang that bai.");
				}
			}

			String maHopDong = timHopDongDangHieuLucTheoPhong(con, maPhong.trim());
			if (maHopDong == null) {
				throw new SQLException("Phong chua co hop dong dang hieu luc.");
			}

			// vaiTro = 1: Thanh vien
			themLienKetHopDongKhachHang(con, maHopDong, khachHang.getMaKhachHang(), 1);
			tangSoNguoiPhong(con, maPhong.trim());

			con.commit();
			return khachHang;
		} catch (SQLException | RuntimeException e) {
			if (con != null) {
				try {
					con.rollback();
				} catch (SQLException ignored) {
				}
			}
			throw new RuntimeException("Loi khi them khach hang vao phong da thue.", e);
		} finally {
			if (con != null) {
				try {
					con.setAutoCommit(true);
				} catch (SQLException ignored) {
				}
			}
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

	public String layMaPhongHienTaiTheoKhach(String maKhachHang) {
		String sql = "SELECT TOP 1 hd.maPhong FROM HopDongKhachHang hdkh "
				+ "JOIN HopDong hd ON hdkh.maHopDong = hd.maHopDong "
				+ "WHERE hdkh.maKhachHang = ? AND hd.trangThai = 1 AND hdkh.vaiTro <> 2 "
				+ "ORDER BY hd.ngayBatDau DESC";

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, maKhachHang);
				try (ResultSet rs = ps.executeQuery()) {
					return rs.next() ? rs.getString("maPhong") : "";
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi lay phong cua khach hang.", e);
		}
	}

	public String layMaPhongCuoiCungTheoKhach(String maKhachHang) {
		String sql = "SELECT TOP 1 hd.maPhong FROM HopDongKhachHang hdkh "
				+ "JOIN HopDong hd ON hdkh.maHopDong = hd.maHopDong "
				+ "WHERE hdkh.maKhachHang = ? "
				+ "ORDER BY hd.ngayBatDau DESC";

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, maKhachHang);
				try (ResultSet rs = ps.executeQuery()) {
					return rs.next() ? rs.getString("maPhong") : "";
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi lay phong cuoi cung cua khach hang.", e);
		}
	}

	public boolean kiemTraDaRoiDi(String maKhachHang) {
		String sqlDangO = "SELECT TOP 1 1 FROM HopDongKhachHang hdkh "
				+ "JOIN HopDong hd ON hdkh.maHopDong = hd.maHopDong "
				+ "WHERE hdkh.maKhachHang = ? AND hd.trangThai = 1 AND hdkh.vaiTro <> 2";

		String sqlDaTungThamGia = "SELECT TOP 1 1 FROM HopDongKhachHang WHERE maKhachHang = ?";
		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement psDangO = con.prepareStatement(sqlDangO)) {
				psDangO.setString(1, maKhachHang);
				try (ResultSet rsDangO = psDangO.executeQuery()) {
					if (rsDangO.next()) {
						return false;
					}
				}
			}

			try (PreparedStatement psDaTungThamGia = con.prepareStatement(sqlDaTungThamGia)) {
				psDaTungThamGia.setString(1, maKhachHang);
				try (ResultSet rsDaTungThamGia = psDaTungThamGia.executeQuery()) {
					return rsDaTungThamGia.next();
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi kiem tra trang thai khach hang.", e);
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
		Connection con = null;
		try {
			con = connectDB.getConnection();
			con.setAutoCommit(false);

			Set<String> dsPhongLienQuan = new HashSet<>();
			String sqlPhong = "SELECT hd.maPhong FROM HopDongKhachHang hdkh "
					+ "JOIN HopDong hd ON hdkh.maHopDong = hd.maHopDong WHERE hdkh.maKhachHang = ?";
			try (PreparedStatement ps = con.prepareStatement(sqlPhong)) {
				ps.setString(1, maKhachHang);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						dsPhongLienQuan.add(rs.getString("maPhong"));
					}
				}
			}

			String sqlXoaLienKet = "DELETE FROM HopDongKhachHang WHERE maKhachHang = ?";
			try (PreparedStatement ps = con.prepareStatement(sqlXoaLienKet)) {
				ps.setString(1, maKhachHang);
				ps.executeUpdate();
			}

			String sqlGiamNguoi = "UPDATE Phong SET soNguoiHienTai = CASE "
					+ "WHEN ISNULL(soNguoiHienTai, 0) - 1 < 0 THEN 0 "
					+ "ELSE ISNULL(soNguoiHienTai, 0) - 1 END WHERE maPhong = ?";
			try (PreparedStatement ps = con.prepareStatement(sqlGiamNguoi)) {
				for (String maPhong : dsPhongLienQuan) {
					ps.setString(1, maPhong);
					ps.executeUpdate();
				}
			}

			String sqlXoaKhach = "DELETE FROM KhachHang WHERE maKhachHang = ?";
			int soDong;
			try (PreparedStatement ps = con.prepareStatement(sqlXoaKhach)) {
				ps.setString(1, maKhachHang);
				soDong = ps.executeUpdate();
			}

			con.commit();
			return soDong > 0;
		} catch (SQLException e) {
			if (con != null) {
				try {
					con.rollback();
				} catch (SQLException ignored) {
				}
			}
			throw new RuntimeException("Loi khi xoa khach hang.", e);
		} finally {
			if (con != null) {
				try {
					con.setAutoCommit(true);
				} catch (SQLException ignored) {
				}
			}
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

	public boolean kiemTraCCCDTonTai(String soCCCD, String maKhachHangHienTai) {
		String sql = "SELECT COUNT(*) FROM KhachHang WHERE soCCCD = ?" +
				(maKhachHangHienTai != null ? " AND maKhachHang != ?" : "");
		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, soCCCD);
				if (maKhachHangHienTai != null) {
					ps.setString(2, maKhachHangHienTai);
				}
				try (ResultSet rs = ps.executeQuery()) {
					return rs.next() && rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi kiem tra CCCD trung.", e);
		}
	}

}
