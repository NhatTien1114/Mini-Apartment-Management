package dao;

import database.connectDB;
import entity.Chu;
import entity.Toa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ToaDAO {

	public List<Toa> layDanhSachToa() {
		String sql = "SELECT maToa, tenToa, chuSoHuu FROM Toa ORDER BY maToa";
		List<Toa> ketQua = new ArrayList<>();

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ketQua.add(mapToa(rs));
				}
			}
			return ketQua;
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi lay danh sach toa.", e);
		}
	}

	public Toa timTheoMa(String maToa) {
		String sql = "SELECT maToa, tenToa, chuSoHuu FROM Toa WHERE maToa = ?";

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, maToa);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						return mapToa(rs);
					}
					return null;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi tim toa theo ma.", e);
		}
	}

	public boolean tonTai(String maToa) {
		String sql = "SELECT 1 FROM Toa WHERE maToa = ?";
		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, maToa);
				try (ResultSet rs = ps.executeQuery()) {
					return rs.next();
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi kiem tra toa ton tai.", e);
		}
	}

	public Toa themToa(Toa toa) {
		String sql = "INSERT INTO Toa(maToa, tenToa, chuSoHuu) VALUES (?, ?, ?)";

		try {
			Connection con = connectDB.getConnection();
			int soDong;
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, toa.getMaToa());
				ps.setString(2, toa.getTenToa());
				ps.setString(3, toa.getChuHo().getMaChu());
				soDong = ps.executeUpdate();
			}

			if (soDong == 0) {
				throw new RuntimeException("Them toa that bai.");
			}

			return toa;
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi them toa vao database.", e);
		}
	}

	public boolean capNhatToa(Toa toa) {
		String sql = "UPDATE Toa SET tenToa = ?, chuSoHuu = ? WHERE maToa = ?";

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, toa.getTenToa());
				ps.setString(2, toa.getChuHo().getMaChu());
				ps.setString(3, toa.getMaToa());
				return ps.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi cap nhat toa.", e);
		}
	}

	public boolean xoaToa(String maToa) {
		String sql = "DELETE FROM Toa WHERE maToa = ?";

		try {
			Connection con = connectDB.getConnection();
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, maToa);
				return ps.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Loi khi xoa toa.", e);
		}
	}

	private Toa mapToa(ResultSet rs) throws SQLException {
		Chu chu = new Chu();
		chu.setMaChu(rs.getString("chuSoHuu"));
		return new Toa(rs.getString("maToa"), rs.getString("tenToa"), chu);
	}
}
