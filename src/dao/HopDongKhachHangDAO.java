package dao;

import database.connectDB;
import entity.KhachHang;

import java.sql.*;
import java.time.LocalDate;

public class HopDongKhachHangDAO {

    public KhachHang getNguoiDaiDienByMaPhong(String maPhong) {
        // 1. Kiểm tra đầu vào tránh NullPointerException
        if (maPhong == null || maPhong.trim().isEmpty()) {
            return null;
        }

        String ma = maPhong;

        String sql = "SELECT k.* FROM KhachHang k " +
                "JOIN HopDongKhachHang hdkh ON k.maKhachHang = hdkh.maKhachHang " +
                "JOIN HopDong hd ON hdkh.maHopDong = hd.maHopDong " +
                "WHERE hd.maPhong = ? AND hdkh.vaiTro = 0 AND hd.trangThai = 1";

        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ma);

            try (ResultSet rs = ps.executeQuery()) {
                KhachHang kh = null;
                if (rs.next()) {
                    String maKH = rs.getString("maKhachHang");
                    String hoTen = rs.getString("hoTen");
                    String SDT = rs.getString("soDienThoai");
                    LocalDate ngaySinh = rs.getObject("ngaySinh", LocalDate.class);
                    String CCCD = rs.getString("soCCCD");
                    String diaChi = rs.getString("diaChiThuongTru");

                    kh = new KhachHang(maKH, hoTen, SDT, ngaySinh, CCCD, diaChi);

                }
                return kh;
            }
        } catch (SQLException e) {
            // In ra lỗi chi tiết để dễ debug
            e.printStackTrace();
            return null;
        }
    }
}
