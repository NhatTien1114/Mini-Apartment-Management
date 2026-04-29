package dao;

import database.connectDB;
import entity.ChiSoDienNuoc;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class ChiSoDienNuocDAO {

    private ChiSoDienNuoc mapRow(ResultSet rs) throws SQLException {
        int maChiSo = rs.getInt("maChiSo");
        String maHopDong = rs.getString("maHopDong");
        LocalDate ngayGhi = rs.getDate("ngayGhi").toLocalDate();
        int soDien = rs.getInt("soDien");
        int soNuoc = rs.getInt("soNuoc");
        return new ChiSoDienNuoc(maChiSo, maHopDong, ngayGhi, soDien, soNuoc);
    }

    public ArrayList<ChiSoDienNuoc> getAllChiSoThang(int thang, int nam) {
        ArrayList<ChiSoDienNuoc> ds = new ArrayList<>();
        String sql = "SELECT * FROM ChiSoDienNuoc "
                + "WHERE MONTH(ngayGhi) = ? AND YEAR(ngayGhi) = ? "
                + "ORDER BY maHopDong ASC, ngayGhi ASC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public ArrayList<ChiSoDienNuoc> getChiSoThangTruoc(int thang, int nam) {
        int thangTruoc = thang - 1;
        int namTruoc = nam;
        if (thangTruoc == 0) {
            thangTruoc = 12;
            namTruoc = nam - 1;
        }
        return getAllChiSoThang(thangTruoc, namTruoc);
    }

    /**
     * Lấy chỉ số điện/nước gần nhất TRƯỚC ngày chỉ định của một hợp đồng.
     * Trả về int[]{soDien, soNuoc}, hoặc {0,0} nếu chưa có dữ liệu.
     */
    public int[] layChiSoTruocNgay(String maHopDong, LocalDate ngay) {
        String sql = "SELECT TOP 1 soDien, soNuoc FROM ChiSoDienNuoc "
                + "WHERE maHopDong = ? AND ngayGhi < ? "
                + "ORDER BY ngayGhi DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHopDong);
            ps.setDate(2, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[]{rs.getInt("soDien"), rs.getInt("soNuoc")};
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiSoTruocNgay: " + e.getMessage());
        }
        return new int[]{0, 0};
    }

    /**
     * Lấy chỉ số điện/nước gần nhất TRƯỚC tháng hiện tại của một hợp đồng.
     * Trả về int[]{soDien, soNuoc}, hoặc {0,0} nếu chưa có dữ liệu.
     */
    public int[] layChiSoThangTruoc(String maHopDong, int thangHienTai, int namHienTai) {
        LocalDate dauThang = LocalDate.of(namHienTai, thangHienTai, 1);
        return layChiSoTruocNgay(maHopDong, dauThang);
    }

    /**
     * Lấy chỉ số điện/nước mới nhất trong một tháng của một hợp đồng.
     * Trả về int[]{soDien, soNuoc}, hoặc null nếu chưa có.
     */
    public int[] layChiSoTheoThang(String maHopDong, int thang, int nam) {
        String sql = "SELECT TOP 1 soDien, soNuoc FROM ChiSoDienNuoc "
                + "WHERE maHopDong = ? AND MONTH(ngayGhi) = ? AND YEAR(ngayGhi) = ? "
                + "ORDER BY ngayGhi DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHopDong);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[]{rs.getInt("soDien"), rs.getInt("soNuoc")};
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiSoTheoThang: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy chỉ số mới nhất trong tháng kèm ngày ghi.
     * Trả về ChiSoDienNuoc (đầy đủ), hoặc null nếu chưa có.
     */
    public ChiSoDienNuoc layChiSoTheoThangVoiNgay(String maHopDong, int thang, int nam) {
        String sql = "SELECT TOP 1 * FROM ChiSoDienNuoc "
                + "WHERE maHopDong = ? AND MONTH(ngayGhi) = ? AND YEAR(ngayGhi) = ? "
                + "ORDER BY ngayGhi DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHopDong);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiSoTheoThangVoiNgay: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy chỉ số điện/nước mới nhất của một hợp đồng.
     * Trả về int[]{soDien, soNuoc}, hoặc {0,0} nếu chưa có dữ liệu.
     */
    public int[] layChiSoGanNhat(String maHopDong) {
        String sql = "SELECT TOP 1 soDien, soNuoc FROM ChiSoDienNuoc "
                + "WHERE maHopDong = ? ORDER BY ngayGhi DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHopDong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[]{rs.getInt("soDien"), rs.getInt("soNuoc")};
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiSoGanNhat: " + e.getMessage());
        }
        return new int[]{0, 0};
    }

    /**
     * Lưu hoặc cập nhật chỉ số theo khóa duy nhất (maHopDong, ngayGhi).
     * Trả về null nếu thành công, chuỗi lỗi nếu thất bại.
     */
    public String luuHoacCapNhat(ChiSoDienNuoc cs) {
        String sqlCheck = "SELECT 1 FROM ChiSoDienNuoc WHERE maHopDong = ? AND ngayGhi = ?";
        String sqlUpdate = "UPDATE ChiSoDienNuoc SET soDien = ?, soNuoc = ? "
                + "WHERE maHopDong = ? AND ngayGhi = ?";
        String sqlInsert = "INSERT INTO ChiSoDienNuoc(maHopDong, ngayGhi, soDien, soNuoc) "
                + "VALUES(?, ?, ?, ?)";

        try (Connection con = connectDB.getConnection()) {
            boolean exists;
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setString(1, cs.getMaHopDong());
                psCheck.setDate(2, Date.valueOf(cs.getNgayGhi()));
                try (ResultSet rs = psCheck.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement psUpd = con.prepareStatement(sqlUpdate)) {
                    psUpd.setInt(1, cs.getSoDien());
                    psUpd.setInt(2, cs.getSoNuoc());
                    psUpd.setString(3, cs.getMaHopDong());
                    psUpd.setDate(4, Date.valueOf(cs.getNgayGhi()));
                    psUpd.executeUpdate();
                }
            } else {
                try (PreparedStatement psIns = con.prepareStatement(sqlInsert)) {
                    psIns.setString(1, cs.getMaHopDong());
                    psIns.setDate(2, Date.valueOf(cs.getNgayGhi()));
                    psIns.setInt(3, cs.getSoDien());
                    psIns.setInt(4, cs.getSoNuoc());
                    psIns.executeUpdate();
                }
            }
            return null;
        } catch (SQLException e) {
            return "Lỗi lưu chỉ số điện nước: " + e.getMessage();
        }
    }
}
