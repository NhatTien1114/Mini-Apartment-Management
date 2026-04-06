package dao;

import database.connectDB;
import entity.LoaiPhong;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoaiPhongDAO {

    public List<LoaiPhong> layTatCa() {
        List<LoaiPhong> ds = new ArrayList<>();
        String sql = "SELECT maLoaiPhong, tenLoaiPhong FROM LoaiPhong ORDER BY maLoaiPhong";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new LoaiPhong(rs.getInt("maLoaiPhong"), rs.getNString("tenLoaiPhong")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public LoaiPhong layTheoMa(int ma) {
        String sql = "SELECT maLoaiPhong, tenLoaiPhong FROM LoaiPhong WHERE maLoaiPhong = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, ma);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new LoaiPhong(rs.getInt("maLoaiPhong"), rs.getNString("tenLoaiPhong"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String them(String tenLoaiPhong) {
        String sql = "INSERT INTO LoaiPhong (tenLoaiPhong) VALUES (?)";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setNString(1, tenLoaiPhong);
                ps.executeUpdate();
                return null;
            }
        } catch (SQLException e) {
            return "Lỗi: " + e.getMessage();
        }
    }

    public String capNhat(int ma, String tenLoaiPhong) {
        String sql = "UPDATE LoaiPhong SET tenLoaiPhong = ? WHERE maLoaiPhong = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setNString(1, tenLoaiPhong);
                ps.setInt(2, ma);
                ps.executeUpdate();
                return null;
            }
        } catch (SQLException e) {
            return "Lỗi: " + e.getMessage();
        }
    }

    public String xoa(int ma) {
        String checkSql = "SELECT COUNT(*) FROM Phong WHERE loaiPhong = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement psChk = con.prepareStatement(checkSql)) {
                psChk.setInt(1, ma);
                try (ResultSet rs = psChk.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return "Không thể xóa. Đang có phòng sử dụng loại phòng này.";
                    }
                }
            }
            // Also check GiaDetail
            String checkGia = "SELECT COUNT(*) FROM GiaDetail WHERE loaiPhong = ?";
            try (PreparedStatement psChk2 = con.prepareStatement(checkGia)) {
                psChk2.setInt(1, ma);
                try (ResultSet rs = psChk2.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return "Không thể xóa. Đang có bảng giá sử dụng loại phòng này.";
                    }
                }
            }
            String sql = "DELETE FROM LoaiPhong WHERE maLoaiPhong = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, ma);
                ps.executeUpdate();
            }
            return null;
        } catch (SQLException e) {
            return "Lỗi: " + e.getMessage();
        }
    }
}
