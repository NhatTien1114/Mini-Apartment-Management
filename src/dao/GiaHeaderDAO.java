package dao;

import database.connectDB;
import entity.GiaHeader;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GiaHeaderDAO {

    public String them(GiaHeader gh) {
        if (gh.getMaGiaHeader() == null || gh.getMaGiaHeader().trim().isEmpty())
            return "Mã bảng giá không được để trống";
        if (gh.getNgayBatDau() == null)
            return "Ngày bắt đầu không được để trống";
        if (gh.getLoai() < 0 || gh.getLoai() > 1)
            return "Loại không hợp lệ (0=Phòng, 1=Dịch vụ)";

        String sql = "INSERT INTO GiaHeader(maGiaHeader, ngayBatDau, ngayKetThuc, moTa, trangThai, loai, ghiChu) "
                   + "VALUES(?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = connectDB.getConnection();
            if (tonTai(gh.getMaGiaHeader())) {
                return "Mã bảng giá \"" + gh.getMaGiaHeader() + "\" đã tồn tại";
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, gh.getMaGiaHeader());
                ps.setDate(2, java.sql.Date.valueOf(gh.getNgayBatDau()));
                ps.setDate(3, gh.getNgayKetThuc() != null ? java.sql.Date.valueOf(gh.getNgayKetThuc()) : null);
                ps.setString(4, gh.getMoTa());
                ps.setInt(5, gh.getTrangThai());
                ps.setInt(6, gh.getLoai());
                ps.setString(7, gh.getGhiChu());
                int rows = ps.executeUpdate();
                if (rows == 0)
                    return "Không thể thêm bảng giá vào database";
            }
            return null;
        } catch (SQLException e) {
            return "Lỗi database khi thêm: " + e.getMessage();
        }
    }

    public GiaHeader layTheoMa(String maGiaHeader) {
        String sql = "SELECT * FROM GiaHeader WHERE maGiaHeader = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maGiaHeader);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToGiaHeader(rs);
                    }
                }
            }
        } catch (SQLException e) {
            // Silent
        }
        return null;
    }

    public List<GiaHeader> layTatCa() {
        String sql = "SELECT * FROM GiaHeader ORDER BY maGiaHeader";
        List<GiaHeader> result = new ArrayList<>();
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapResultSetToGiaHeader(rs));
                }
            }
        } catch (SQLException e) {
            // Silent
        }
        return result;
    }

    public List<GiaHeader> layTheoLoai(int loai) {
        String sql = "SELECT * FROM GiaHeader WHERE loai = ? ORDER BY maGiaHeader";
        List<GiaHeader> result = new ArrayList<>();
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, loai);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(mapResultSetToGiaHeader(rs));
                    }
                }
            }
        } catch (SQLException e) {
            // Silent
        }
        return result;
    }

    public String capNhat(GiaHeader gh) {
        String sql = "UPDATE GiaHeader SET ngayBatDau = ?, ngayKetThuc = ?, moTa = ?, trangThai = ?, loai = ?, ghiChu = ? "
                   + "WHERE maGiaHeader = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDate(1, java.sql.Date.valueOf(gh.getNgayBatDau()));
                ps.setDate(2, gh.getNgayKetThuc() != null ? java.sql.Date.valueOf(gh.getNgayKetThuc()) : null);
                ps.setString(3, gh.getMoTa());
                ps.setInt(4, gh.getTrangThai());
                ps.setInt(5, gh.getLoai());
                ps.setString(6, gh.getGhiChu());
                ps.setString(7, gh.getMaGiaHeader());
                int rows = ps.executeUpdate();
                if (rows == 0)
                    return "Không tìm thấy bảng giá";
            }
            return null;
        } catch (SQLException e) {
            return "Lỗi database: " + e.getMessage();
        }
    }

    public String xoa(String maGiaHeader) {
        String sql = "DELETE FROM GiaHeader WHERE maGiaHeader = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maGiaHeader);
                int rows = ps.executeUpdate();
                if (rows == 0)
                    return "Không tìm thấy bảng giá";
            }
            return null;
        } catch (SQLException e) {
            return "Lỗi database: " + e.getMessage();
        }
    }

    public boolean tonTai(String maGiaHeader) {
        String sql = "SELECT 1 FROM GiaHeader WHERE maGiaHeader = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maGiaHeader);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            // Silent
        }
        return false;
    }

    private GiaHeader mapResultSetToGiaHeader(ResultSet rs) throws SQLException {
        GiaHeader gh = new GiaHeader();
        gh.setMaGiaHeader(rs.getString("maGiaHeader"));
        gh.setNgayBatDau(rs.getDate("ngayBatDau").toLocalDate());
        Date ngayKetThuc = rs.getDate("ngayKetThuc");
        if (ngayKetThuc != null)
            gh.setNgayKetThuc(ngayKetThuc.toLocalDate());
        gh.setMoTa(rs.getString("moTa"));
        gh.setTrangThai(rs.getInt("trangThai"));
        gh.setLoai(rs.getInt("loai"));
        gh.setGhiChu(rs.getString("ghiChu"));
        return gh;
    }
}
