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
        String hoTen = "";
        // Chuẩn hóa mã phòng (ví dụ: " p101 " -> "P101")
        String ma = normalise(maPhong);

        // SQL: Chỉ SELECT cột hoTen, JOIN qua bảng trung gian
        String sql = "SELECT k.hoTen FROM KhachHang k " +
                "JOIN HopDongKhachHang hdkh ON k.maKhachHang = hdkh.maKhachHang " +
                "JOIN HopDong hd ON hdkh.maHopDong = hd.maHopDong " +
                "WHERE hd.maPhong = ? AND hdkh.vaiTro = 0 AND hd.trangThai = 1";

        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ma);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    hoTen = rs.getString("hoTen");
                } else {
                    hoTen = "Chưa có"; // Trả về thông báo nếu không tìm thấy
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi.", e);
        }
        return hoTen;
    }
}
