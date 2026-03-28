package dao;

import database.connectDB;
import entity.DichVu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {

    public List<DichVu> layTatCa() {
        String sql = "SELECT d.maDichVu, d.tenDichVu, d.donVi, d.maGiaDetail, gd.donGia " +
                     "FROM DichVu d " +
                     "LEFT JOIN GiaDetail gd ON d.maGiaDetail = gd.maGiaDetail " +
                     "ORDER BY d.tenDichVu";
                     
        List<DichVu> ds = new ArrayList<>();

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new DichVu(
                            rs.getString("maDichVu"),
                            rs.getString("tenDichVu"),
                            rs.getString("donVi"),
                            rs.getString("maGiaDetail"),
                            rs.getObject("donGia") != null ? rs.getDouble("donGia") : null
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTatCa DichVu: " + e.getMessage());
        }

        return ds;
    }

    public boolean insertDichVu(DichVu dv) {
        String sqlCount = "SELECT COUNT(*) FROM DichVu";
        String sqlInsert = "INSERT INTO DichVu (maDichVu, tenDichVu, donVi) VALUES (?, ?, ?)";
        
        try {
            Connection con = connectDB.getConnection();
            // 1. Phát sinh mã
            int count = 0;
            try (PreparedStatement psCount = con.prepareStatement(sqlCount);
                 ResultSet rsCount = psCount.executeQuery()) {
                if (rsCount.next()) {
                    count = rsCount.getInt(1);
                }
            }
            String newMa = String.format("DV%02d", count + 1);
            dv.setMaDichVu(newMa);

            // 2. Insert
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                psInsert.setString(1, dv.getMaDichVu());
                psInsert.setString(2, dv.getTenDichVu());
                psInsert.setString(3, dv.getDonVi());
                return psInsert.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi insert DichVu: " + e.getMessage());
            return false;
        }
    }

    public boolean updateDichVu(DichVu dv) {
        String sql = "UPDATE DichVu SET tenDichVu = ?, donVi = ? WHERE maDichVu = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, dv.getTenDichVu());
                ps.setString(2, dv.getDonVi());
                ps.setString(3, dv.getMaDichVu());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi update DichVu: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteDichVu(String maDichVu) {
        String sql = "DELETE FROM DichVu WHERE maDichVu = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maDichVu);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi delete DichVu: " + e.getMessage());
            return false;
        }
    }

}