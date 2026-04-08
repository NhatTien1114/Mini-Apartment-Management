package dao;

import database.connectDB;
import entity.PhuongTien;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhuongTienDAO {

    public List<PhuongTien> getAllPhuongTien() {
        String sql = "SELECT pt.bienSo, pt.loaiXe, pt.maKhachHang, kh.hoTen, pt.maPhong, pt.mucPhi " +
                     "FROM PhuongTien pt " +
                     "LEFT JOIN KhachHang kh ON pt.maKhachHang = kh.maKhachHang " +
                     "ORDER BY pt.maPhong, pt.bienSo";
        List<PhuongTien> ds = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PhuongTien pt = new PhuongTien(
                    rs.getString("bienSo"),
                    rs.getString("loaiXe"),
                    rs.getString("maKhachHang"),
                    rs.getString("hoTen"),
                    rs.getString("maPhong"),
                    rs.getDouble("mucPhi")
                );
                ds.add(pt);
            }
        } catch (SQLException e) {
            System.err.println("Loi getAllPhuongTien: " + e.getMessage());
        }
        return ds;
    }

    public boolean insert(PhuongTien pt) {
        String sql = "INSERT INTO PhuongTien (bienSo, loaiXe, maKhachHang, maPhong, mucPhi) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pt.getBienSo());
            ps.setString(2, pt.getLoaiXe());
            ps.setString(3, pt.getMaKhachHang());
            ps.setString(4, pt.getMaPhong());
            ps.setDouble(5, pt.getMucPhi());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi insert PhuongTien: " + e.getMessage());
            return false;
        }
    }

    public boolean update(PhuongTien pt) {
        String sql = "UPDATE PhuongTien SET loaiXe=?, maKhachHang=?, maPhong=?, mucPhi=? WHERE bienSo=?";
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pt.getLoaiXe());
            ps.setString(2, pt.getMaKhachHang());
            ps.setString(3, pt.getMaPhong());
            ps.setDouble(4, pt.getMucPhi());
            ps.setString(5, pt.getBienSo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi update PhuongTien: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String bienSo) {
        String sql = "DELETE FROM PhuongTien WHERE bienSo=?";
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bienSo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi delete PhuongTien: " + e.getMessage());
            return false;
        }
    }
}
