package dao;

import database.connectDB;
import entity.DichVu;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhongDichVuDAO {

    /**
     * Lưu danh sách dịch vụ đã chọn cho phòng.
     * Xóa hết bản ghi cũ rồi insert lại (chỉ dịch vụ tùy chọn, KHÔNG bao gồm Điện/Nước).
     * Điện và Nước là mặc định, không cần lưu vì luôn hiển thị.
     * @return null nếu thành công, chuỗi lỗi nếu thất bại
     */
    public String luuDichVuPhong(String maPhong, List<String> dsMaDichVu) {
        String sqlDelete = "DELETE FROM PhongDichVu WHERE maPhong = ?";
        String sqlInsert = "INSERT INTO PhongDichVu(maPhong, maDichVu) VALUES(?, ?)";

        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement psDel = con.prepareStatement(sqlDelete)) {
                    psDel.setString(1, maPhong);
                    psDel.executeUpdate();
                }

                if (dsMaDichVu != null && !dsMaDichVu.isEmpty()) {
                    try (PreparedStatement psIns = con.prepareStatement(sqlInsert)) {
                        for (String maDichVu : dsMaDichVu) {
                            psIns.setString(1, maPhong);
                            psIns.setString(2, maDichVu);
                            psIns.addBatch();
                        }
                        psIns.executeBatch();
                    }
                }

                con.commit();
                return null;
            } catch (SQLException e) {
                con.rollback();
                return "Lỗi lưu dịch vụ phòng: " + e.getMessage();
            }
        } catch (SQLException e) {
            return "Lỗi kết nối DB: " + e.getMessage();
        }
    }

    /**
     * Lấy danh sách DichVu (kèm giá từ bảng giá active) của một phòng.
     * Không bao gồm Điện/Nước vì chúng luôn hiển thị mặc định ở UI.
     */
    public List<DichVu> layDichVuCuaPhong(String maPhong) {
        String sql =
                "SELECT d.maDichVu, d.tenDichVu, d.donVi, gd.donGia " +
                        "FROM PhongDichVu pd " +
                        "JOIN DichVu d ON pd.maDichVu = d.maDichVu " +
                        "LEFT JOIN GiaDetail gd ON d.maGiaDetail = gd.maGiaDetail " +
                        "LEFT JOIN GiaHeader gh ON gd.maGiaHeader = gh.maGiaHeader " +
                        "    AND gh.trangThai = 1 " +
                        "WHERE pd.maPhong = ? " +
                        "ORDER BY d.tenDichVu";

        List<DichVu> result = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DichVu dv = new DichVu(
                            rs.getString("maDichVu"),
                            rs.getString("tenDichVu"),
                            rs.getString("donVi"),
                            null,
                            rs.getObject("donGia") != null ? rs.getDouble("donGia") : null
                    );
                    result.add(dv);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layDichVuCuaPhong: " + e.getMessage());
        }
        return result;
    }

    /**
     * Lấy danh sách mã dịch vụ đã chọn của phòng (dùng để restore checkbox khi mở dialog).
     */
    public List<String> layMaDichVuCuaPhong(String maPhong) {
        String sql = "SELECT maDichVu FROM PhongDichVu WHERE maPhong = ?";
        List<String> result = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(rs.getString("maDichVu"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layMaDichVuCuaPhong: " + e.getMessage());
        }
        return result;
    }
}