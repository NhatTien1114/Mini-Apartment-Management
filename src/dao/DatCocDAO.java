package dao;

import database.connectDB;
import entity.DatCoc;
import java.sql.*;
import java.time.LocalDate;

public class DatCocDAO {

    public DatCocDAO() {
        ensureTable();
    }

    private void ensureTable() {
        String sql = "IF OBJECT_ID('dbo.DatCoc', 'U') IS NULL "
                + "BEGIN "
                + "CREATE TABLE dbo.DatCoc ("
                + "  maDatCoc INT IDENTITY(1,1) PRIMARY KEY,"
                + "  maPhong NVARCHAR(20) NOT NULL,"
                + "  hoTen NVARCHAR(100) NOT NULL,"
                + "  soCCCD NVARCHAR(20) NOT NULL,"
                + "  soTien FLOAT NOT NULL DEFAULT 0,"
                + "  ngayDatCoc DATE NOT NULL,"
                + "  soNgay INT NOT NULL DEFAULT 3,"
                + "  CONSTRAINT FK_DatCoc_Phong FOREIGN KEY(maPhong) REFERENCES dbo.Phong(maPhong)"
                + ");"
                + "END";
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            System.err.println("Lỗi ensureTable DatCoc: " + e.getMessage());
        }
    }

    /**
     * Thêm mới đặt cọc.
     */
    public boolean them(DatCoc dc) {
        String sql = "INSERT INTO DatCoc(maPhong, hoTen, soCCCD, soTien, ngayDatCoc, soNgay) "
                + "VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dc.getMaPhong());
            ps.setString(2, dc.getHoTen());
            ps.setString(3, dc.getSoCCCD());
            ps.setDouble(4, dc.getSoTien());
            ps.setDate(5, Date.valueOf(dc.getNgayDatCoc()));
            ps.setInt(6, dc.getSoNgay());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm DatCoc: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy thông tin đặt cọc hiện tại (chưa hết hạn) của một phòng.
     */
    public DatCoc layTheoPhong(String maPhong) {
        String sql = "SELECT TOP 1 * FROM DatCoc WHERE maPhong = ? ORDER BY ngayDatCoc DESC";
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTheoPhong DatCoc: " + e.getMessage());
        }
        return null;
    }

    /**
     * Xóa đặt cọc khi đã lập hợp đồng hoặc hết hạn.
     */
    public boolean xoaTheoPhong(String maPhong) {
        String sql = "DELETE FROM DatCoc WHERE maPhong = ?";
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa DatCoc: " + e.getMessage());
            return false;
        }
    }

    private DatCoc mapRow(ResultSet rs) throws SQLException {
        DatCoc dc = new DatCoc();
        dc.setMaDatCoc(rs.getInt("maDatCoc"));
        dc.setMaPhong(rs.getString("maPhong"));
        dc.setHoTen(rs.getString("hoTen"));
        dc.setSoCCCD(rs.getString("soCCCD"));
        dc.setSoTien(rs.getDouble("soTien"));
        dc.setNgayDatCoc(rs.getDate("ngayDatCoc").toLocalDate());
        dc.setSoNgay(rs.getInt("soNgay"));
        return dc;
    }
}
