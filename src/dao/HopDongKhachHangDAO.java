package dao;

import database.connectDB;
import entity.HopDongKhachThue;
import entity.KhachHang;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class HopDongKhachHangDAO {

    public KhachHang getNguoiDaiDienByMaHopDong(String maHopDong) {
        if (maHopDong == null || maHopDong.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT k.* FROM KhachHang k " +
                "JOIN HopDongKhachHang hdkh ON k.maKhachHang = hdkh.maKhachHang " +
                "WHERE hdkh.maHopDong = ? AND hdkh.vaiTro = 0";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHopDong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapKhachHang(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public KhachHang getNguoiDaiDienByMaPhong(String maPhong) {
        if (maPhong == null || maPhong.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT k.* FROM KhachHang k " +
                "JOIN HopDongKhachHang hdkh ON k.maKhachHang = hdkh.maKhachHang " +
                "JOIN HopDong hd ON hdkh.maHopDong = hd.maHopDong " +
                "WHERE hd.maPhong = ? AND hdkh.vaiTro = 0 AND hd.trangThai = 1";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maPhong);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapKhachHang(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Trả về người đại diện hợp đồng của phòng tại một ngày cụ thể.
     * Dùng để hiển thị đúng tên khách trong chỉ số điện nước theo ngày ghi.
     */
    public KhachHang getNguoiDaiDienByMaPhongTaiNgay(String maPhong, LocalDate ngay) {
        if (maPhong == null || maPhong.trim().isEmpty() || ngay == null) {
            return null;
        }

        String sql = "SELECT TOP 1 k.* FROM KhachHang k " +
                "JOIN HopDongKhachHang hdkh ON k.maKhachHang = hdkh.maKhachHang " +
                "JOIN HopDong hd ON hdkh.maHopDong = hd.maHopDong " +
                "WHERE hd.maPhong = ? AND hdkh.vaiTro = 0 " +
                "AND hd.ngayBatDau <= ? AND (hd.ngayKetThuc >= ? OR hd.trangThai = 1) " +
                "ORDER BY hd.ngayBatDau DESC";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ps.setObject(2, ngay);
            ps.setObject(3, ngay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapKhachHang(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<HopDongKhachThue> getAllThanhVienByMaHopDong(String maHopDong) {
        ArrayList<HopDongKhachThue> list = new ArrayList<>();
        if (maHopDong == null || maHopDong.trim().isEmpty())
            return list;

        String sql = "SELECT hdkh.maHDKT, hdkh.vaiTro, " +
                "k.maKhachHang, k.hoTen, k.soDienThoai, k.ngaySinh, k.soCCCD, k.diaChiThuongTru " +
                "FROM HopDongKhachHang hdkh " +
                "JOIN KhachHang k ON hdkh.maKhachHang = k.maKhachHang " +
                "WHERE hdkh.maHopDong = ? " +
                "ORDER BY hdkh.vaiTro ASC";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHopDong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maHDKT = rs.getString("maHDKT");
                    int vaiTroInt = rs.getInt("vaiTro");
                    KhachHang kh = mapKhachHang(rs);

                    HopDongKhachThue hdkt = new HopDongKhachThue(
                            maHDKT, null, kh,
                            HopDongKhachThue.VaiTro.fromInt(vaiTroInt));
                    list.add(hdkt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean danhDauRoiDi(String maHDKT) {
        String sql = "UPDATE HopDongKhachHang SET vaiTro = 2 WHERE maHDKT = ?";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHDKT);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean doiNguoiDaiDien(String maHopDong, String maHDKT_Moi) {
        Connection con = null;
        try {
            con = connectDB.getConnection();
            con.setAutoCommit(false);

            // Hạ vai trò người đại diện cũ thành "Thành viên"
            String sqlHa = "UPDATE HopDongKhachHang SET vaiTro = 1 " +
                    "WHERE maHopDong = ? AND vaiTro = 0";
            try (PreparedStatement ps = con.prepareStatement(sqlHa)) {
                ps.setString(1, maHopDong);
                ps.executeUpdate();
            }

            // Đặt người đại diện mới
            String sqlLen = "UPDATE HopDongKhachHang SET vaiTro = 0 WHERE maHDKT = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlLen)) {
                ps.setString(1, maHDKT_Moi);
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("Không tìm thấy bản ghi để cập nhật người đại diện.");
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null)
                try {
                    con.rollback();
                } catch (SQLException ignored) {
                }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null)
                try {
                    con.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
        }
    }

    public boolean roiDiVaDoiDaiDien(String maHDKT_RoiDi, String maHopDong, String maHDKT_DaiDienMoi, String maPhong) {
        Connection con = null;
        try {
            con = connectDB.getConnection();
            con.setAutoCommit(false);

            // Đánh dấu người rời đi
            String sqlRoi = "UPDATE HopDongKhachHang SET vaiTro = 2 WHERE maHDKT = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlRoi)) {
                ps.setString(1, maHDKT_RoiDi);
                ps.executeUpdate();
            }

            // Đặt người đại diện mới
            if (maHDKT_DaiDienMoi != null) {
                String sqlLen = "UPDATE HopDongKhachHang SET vaiTro = 0 WHERE maHDKT = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlLen)) {
                    ps.setString(1, maHDKT_DaiDienMoi);
                    ps.executeUpdate();
                }
            }

            // Giảm số người hiện tại
            String sqlGiam = "UPDATE Phong SET soNguoiHienTai = CASE " +
                    "WHEN ISNULL(soNguoiHienTai, 0) - 1 < 0 THEN 0 " +
                    "ELSE soNguoiHienTai - 1 END WHERE maPhong = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlGiam)) {
                ps.setString(1, maPhong);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null)
                try {
                    con.rollback();
                } catch (SQLException ignored) {
                }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null)
                try {
                    con.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
        }
    }

    public boolean thanhVienRoiDi(String maHDKT, String maPhong) {
        Connection con = null;
        try {
            con = connectDB.getConnection();
            con.setAutoCommit(false);

            String sqlRoi = "UPDATE HopDongKhachHang SET vaiTro = 2 WHERE maHDKT = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlRoi)) {
                ps.setString(1, maHDKT);
                ps.executeUpdate();
            }

            String sqlGiam = "UPDATE Phong SET soNguoiHienTai = CASE " +
                    "WHEN ISNULL(soNguoiHienTai, 0) - 1 < 0 THEN 0 " +
                    "ELSE soNguoiHienTai - 1 END WHERE maPhong = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlGiam)) {
                ps.setString(1, maPhong);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null)
                try {
                    con.rollback();
                } catch (SQLException ignored) {
                }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null)
                try {
                    con.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
        }
    }

    private KhachHang mapKhachHang(ResultSet rs) throws SQLException {
        String maKH = rs.getString("maKhachHang");
        String hoTen = rs.getString("hoTen");
        String SDT = rs.getString("soDienThoai");
        LocalDate ngaySinh = rs.getObject("ngaySinh", LocalDate.class);
        String CCCD = rs.getString("soCCCD");
        String diaChi = rs.getString("diaChiThuongTru");
        return new KhachHang(maKH, hoTen, SDT, ngaySinh, CCCD, diaChi);
    }
}
