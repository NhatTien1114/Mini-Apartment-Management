package dao;

import database.connectDB;
import entity.Chu;
import entity.Tang;
import entity.Toa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TangDAO {

	public List<Tang> layDanhSachTang() {
		String sql = "SELECT t.maTang, t.tenTang, t.maToa, toa.tenToa, toa.chuSoHuu "
				+ "FROM Tang t INNER JOIN Toa toa ON t.maToa = toa.maToa ORDER BY t.maTang";
		List<Tang> ketQua = new ArrayList<>();

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ketQua.add(mapTang(rs));
				}
			}
			return ketQua;
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi lay danh sach tang.", e);
		}
	}

	public List<Tang> layTheoToa(String maToa) {
		String sql = "SELECT t.maTang, t.tenTang, t.maToa, toa.tenToa, toa.chuSoHuu "
				+ "FROM Tang t INNER JOIN Toa toa ON t.maToa = toa.maToa "
				+ "WHERE t.maToa = ? ORDER BY t.maTang";
		List<Tang> ketQua = new ArrayList<>();

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, maToa);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						ketQua.add(mapTang(rs));
					}
				}
			}
			return ketQua;
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi lay danh sach tang theo toa.", e);
		}
	}

	public Tang timTheoMa(String maTang) {
		String sql = "SELECT t.maTang, t.tenTang, t.maToa, toa.tenToa, toa.chuSoHuu "
				+ "FROM Tang t INNER JOIN Toa toa ON t.maToa = toa.maToa WHERE t.maTang = ?";

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, maTang);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						return mapTang(rs);
					}
					return null;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi tim tang theo ma.", e);
		}
	}

	public boolean tonTai(String maTang) {
		String sql = "SELECT 1 FROM Tang WHERE maTang = ?";
		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, maTang);
				try (ResultSet rs = ps.executeQuery()) {
					return rs.next();
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi kiem tra tang ton tai.", e);
		}
	}

	public Tang themTang(Tang tang) {
		String sql = "INSERT INTO Tang(maTang, tenTang, maToa) VALUES (?, ?, ?)";

		try {
			Connection con = connectDB.getConnection();
			int soDong;
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, tang.getMaTang());
				ps.setString(2, tang.getTenTang());
				ps.setString(3, tang.getToa().getMaToa());
				soDong = ps.executeUpdate();
			}

			if (soDong == 0) {
				throw new RuntimeException("Them tang that bai.");
			}

			return tang;
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi them tang vao database.", e);
		}
	}

	public boolean capNhatTang(Tang tang) {
		String sql = "UPDATE Tang SET tenTang = ?, maToa = ? WHERE maTang = ?";

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, tang.getTenTang());
				ps.setString(2, tang.getToa().getMaToa());
				ps.setString(3, tang.getMaTang());
				return ps.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi cap nhat tang.", e);
		}
	}

	public boolean xoaTang(String maTang) {
		String sql = "DELETE FROM Tang WHERE maTang = ?";

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, maTang);
				return ps.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi xoa tang.", e);
		}
	}

	private Tang mapTang(ResultSet rs) throws SQLException {
		Chu chu = new Chu();
		chu.setMaChu(rs.getString("chuSoHuu"));
		Toa toa = new Toa(rs.getString("maToa"), rs.getString("tenToa"), chu);
		return new Tang(rs.getString("maTang"), rs.getString("tenTang"), toa);
	}
}
