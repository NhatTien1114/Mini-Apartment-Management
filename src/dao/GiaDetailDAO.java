package dao;

import database.connectDB;
import entity.GiaDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GiaDetailDAO {

    public String them(GiaDetail gd) {
        if (gd.getMaGiaDetail() == null || gd.getMaGiaDetail().trim().isEmpty())
            return "Mã chi tiết giá không được để trống";
        if (gd.getMaGiaHeader() == null || gd.getMaGiaHeader().trim().isEmpty())
            return "Mã bảng giá không được để trống";
        if (gd.getDonGia() < 0)
            return "Đơn giá không được âm";

        String sql = "INSERT INTO GiaDetail(maGiaDetail, maGiaHeader, loaiPhong, maDichVu, donGia) "
                   + "VALUES(?, ?, ?, ?, ?)";

        try {
            Connection con = connectDB.getConnection();
            if (tonTai(gd.getMaGiaDetail()))
                return "Mã chi tiết giá đã tồn tại";
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, gd.getMaGiaDetail());
                ps.setString(2, gd.getMaGiaHeader());
                if (gd.getLoaiPhong() != null)
                    ps.setInt(3, gd.getLoaiPhong());
                else
                    ps.setNull(3, Types.INTEGER);
                
                ps.setString(4, gd.getMaDichVu());
                ps.setDouble(5, gd.getDonGia());
                
                int rows = ps.executeUpdate();
                if (rows == 0)
                    return "Không thể thêm chi tiết giá";
            }
            return null;
        } catch (SQLException e) {
            return "Lỗi database: " + e.getMessage();
        }
    }

    public GiaDetail layTheoMa(String maGiaDetail) {
        String sql = "SELECT * FROM GiaDetail WHERE maGiaDetail = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maGiaDetail);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        return mapResultSetToGiaDetail(rs);
                }
            }
        } catch (SQLException e) {
            // Silent
        }
        return null;
    }

    public List<GiaDetail> layTheoGiaHeader(String maGiaHeader) {
        String sql = "SELECT * FROM GiaDetail WHERE maGiaHeader = ? ORDER BY maGiaDetail";
        List<GiaDetail> result = new ArrayList<>();
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maGiaHeader);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        result.add(mapResultSetToGiaDetail(rs));
                }
            }
        } catch (SQLException e) {
            // Silent
        }
        return result;
    }

    public List<GiaDetail> layTatCa() {
        String sql = "SELECT * FROM GiaDetail ORDER BY maGiaHeader, maGiaDetail";
        List<GiaDetail> result = new ArrayList<>();
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(mapResultSetToGiaDetail(rs));
            }
        } catch (SQLException e) {
            // Silent
        }
        return result;
    }

    public String capNhat(GiaDetail gd) {
        String sql = "UPDATE GiaDetail SET loaiPhong = ?, maDichVu = ?, donGia = ? WHERE maGiaDetail = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                if (gd.getLoaiPhong() != null)
                    ps.setInt(1, gd.getLoaiPhong());
                else
                    ps.setNull(1, Types.INTEGER);
                
                ps.setString(2, gd.getMaDichVu());
                ps.setDouble(3, gd.getDonGia());
                ps.setString(4, gd.getMaGiaDetail());
                
                int rows = ps.executeUpdate();
                if (rows == 0)
                    return "Không tìm thấy chi tiết giá";
            }
            return null;
        } catch (SQLException e) {
            return "Lỗi database: " + e.getMessage();
        }
    }

    public String xoa(String maGiaDetail) {
        String sql = "DELETE FROM GiaDetail WHERE maGiaDetail = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maGiaDetail);
                int rows = ps.executeUpdate();
                if (rows == 0)
                    return "Không tìm thấy chi tiết giá";
            }
            return null;
        } catch (SQLException e) {
            return "Lỗi database: " + e.getMessage();
        }
    }

    public boolean tonTai(String maGiaDetail) {
        String sql = "SELECT 1 FROM GiaDetail WHERE maGiaDetail = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maGiaDetail);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            // Silent
        }
        return false;
    }

    private GiaDetail mapResultSetToGiaDetail(ResultSet rs) throws SQLException {
        GiaDetail gd = new GiaDetail();
        gd.setMaGiaDetail(rs.getString("maGiaDetail"));
        gd.setMaGiaHeader(rs.getString("maGiaHeader"));
        
        int loaiPhong = rs.getInt("loaiPhong");
        gd.setLoaiPhong(rs.wasNull() ? null : loaiPhong);
        
        gd.setMaDichVu(rs.getString("maDichVu"));
        gd.setDonGia(rs.getDouble("donGia"));
        
        return gd;
    }
}
