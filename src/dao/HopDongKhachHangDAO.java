package dao;

import database.connectDB;
import entity.HopDong;
import entity.HopDongKhachThue;
import entity.KhachHang;
import entity.Phong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static dao.QuanLyPhongDAO.normalise;

public class HopDongKhachHangDAO {

    public String getTenNguoiDaiDienByMaPhong(String maPhong) {
        // 1. Kiểm tra đầu vào tránh NullPointerException
        if (maPhong == null || maPhong.trim().isEmpty()) {
            return "Mã phòng trống";
        }

        String hoTen = "Chưa có";
        String ma = maPhong.trim().toUpperCase(); // Chuẩn hóa trực tiếp tại đây

        // 2. Sử dụng UPPER(?) để truy vấn chính xác hơn
        String sql = "SELECT k.hoTen FROM KhachHang k " +
                "JOIN HopDongKhachHang hdkh ON k.maKhachHang = hdkh.maKhachHang " +
                "JOIN HopDong hd ON hdkh.maHopDong = hd.maHopDong " +
                "WHERE UPPER(hd.maPhong) = ? AND hdkh.vaiTro = 0 AND hd.trangThai = 1";

        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ma);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String result = rs.getString("hoTen");
                    if (result != null && !result.isEmpty()) {
                        hoTen = result;
                    }
                }
            }
        } catch (SQLException e) {
            // In ra lỗi chi tiết để dễ debug
            e.printStackTrace();
            return "Lỗi kết nối DB";
        }
        return hoTen;
    }
}
