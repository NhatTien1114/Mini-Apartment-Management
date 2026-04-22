package dao;

import database.connectDB;
import entity.ChiSoDienNuoc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ChiSoDienNuocDAO {

    /**
     * Lấy chỉ số điện/nước của tháng gần nhất TRƯỚC tháng/năm hiện tại.
     * Nếu chưa có dữ liệu nào → trả về [0, 0]
     * 
     * @return int[]{soDien, soNuoc}
     */
    public ArrayList<ChiSoDienNuoc> getAllChiSoThang(String thangHienTai, String namHienTai) {
        ArrayList<ChiSoDienNuoc> dsChiSo = new ArrayList<>();

        String sql = "SELECT * FROM ChiSoDienNuoc WHERE thang = ? AND nam = ? ORDER BY maPhong ASC, ngay ASC";

        try (Connection con = connectDB.getConnection();
                PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, thangHienTai);
            stmt.setString(2, namHienTai);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String maPhong = rs.getString("maPhong");
                    int thang = rs.getInt("thang");
                    int nam = rs.getInt("nam");
                    int ngay = rs.getInt("ngay");
                    int soDien = rs.getInt("soDien");
                    int soNuoc = rs.getInt("soNuoc");

                    ChiSoDienNuoc csdn = new ChiSoDienNuoc(maPhong, thang, nam, ngay, soDien, soNuoc);

                    dsChiSo.add(csdn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsChiSo;
    }

    public ArrayList<ChiSoDienNuoc> getChiSoThangTruoc(String thangHienTai, String namHienTai) {
        int thangTruoc = Integer.parseInt(thangHienTai) - 1;
        int namTruoc = Integer.parseInt(namHienTai);

        if (thangTruoc == 0) {
            thangTruoc = 12;
            namTruoc = Integer.parseInt(namHienTai) - 1;
        }

        ArrayList<ChiSoDienNuoc> dsChiSo = new ArrayList<>();

        String sql = "SELECT * FROM ChiSoDienNuoc WHERE thang = ? AND nam = ? ORDER BY maPhong ASC";

        try (Connection con = connectDB.getConnection();
                PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, thangTruoc);
            stmt.setInt(2, namTruoc);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String maPhong = rs.getString("maPhong");
                    int thang = rs.getInt("thang");
                    int nam = rs.getInt("nam");
                    int ngay = rs.getInt("ngay");
                    int soDien = rs.getInt("soDien");
                    int soNuoc = rs.getInt("soNuoc");

                    ChiSoDienNuoc csdn = new ChiSoDienNuoc(maPhong, thang, nam, ngay, soDien, soNuoc);
                    dsChiSo.add(csdn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsChiSo;
    }

    public int[] layChiSoThangTruoc(String maPhong, int thangHienTai, int namHienTai) {
        String sql = "SELECT TOP 1 soDien, soNuoc FROM ChiSoDienNuoc "
                + "WHERE maPhong = ? "
                + "  AND (nam < ? OR (nam = ? AND thang < ?)) "
                + "ORDER BY nam DESC, thang DESC, ngay DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ps.setInt(2, namHienTai);
            ps.setInt(3, namHienTai);
            ps.setInt(4, thangHienTai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[] { rs.getInt("soDien"), rs.getInt("soNuoc") };
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiSoThangTruoc: " + e.getMessage());
        }
        return new int[] { 0, 0 }; // Tháng đầu tiên → 0
    }

    /**
     * Lấy chỉ số điện/nước gần nhất TRƯỚC (thang, nam, ngay) — bao gồm cả
     * các bản ghi cùng tháng nhưng có ngay nhỏ hơn.
     * Dùng để tính chỉ số cũ khi người mới vào giữa tháng.
     */
    public int[] layChiSoTruocNgay(String maPhong, int thang, int nam, int ngay) {
        String sql = "SELECT TOP 1 soDien, soNuoc FROM ChiSoDienNuoc "
                + "WHERE maPhong = ? "
                + "  AND (nam < ? "
                + "    OR (nam = ? AND thang < ?) "
                + "    OR (nam = ? AND thang = ? AND ngay < ?)) "
                + "ORDER BY nam DESC, thang DESC, ngay DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ps.setInt(2, nam);
            ps.setInt(3, nam);
            ps.setInt(4, thang);
            ps.setInt(5, nam);
            ps.setInt(6, thang);
            ps.setInt(7, ngay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[] { rs.getInt("soDien"), rs.getInt("soNuoc") };
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiSoTruocNgay: " + e.getMessage());
        }
        return new int[] { 0, 0 };
    }

    public int[] layChiSoTheoThang(String maPhong, int thang, int nam) {
        String sql = "SELECT TOP 1 soDien, soNuoc FROM ChiSoDienNuoc "
                + "WHERE maPhong = ? AND thang = ? AND nam = ? ORDER BY ngay DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[] { rs.getInt("soDien"), rs.getInt("soNuoc") };
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiSoTheoThang: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy chỉ số điện/nước mới nhất của tháng, kèm ngày.
     * 
     * @return int[]{ngay, soDien, soNuoc} hoặc null nếu chưa có.
     */
    public int[] layChiSoTheoThangVoiNgay(String maPhong, int thang, int nam) {
        String sql = "SELECT TOP 1 ngay, soDien, soNuoc FROM ChiSoDienNuoc "
                + "WHERE maPhong = ? AND thang = ? AND nam = ? ORDER BY ngay DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[] { rs.getInt("ngay"), rs.getInt("soDien"), rs.getInt("soNuoc") };
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiSoTheoThangVoiNgay: " + e.getMessage());
        }
        return null;
    }

    public int[] layChiSoGanNhat(String maPhong) {
        String sql = "SELECT TOP 1 soDien, soNuoc FROM ChiSoDienNuoc "
                + "WHERE maPhong = ? "
                + "ORDER BY nam DESC, thang DESC, ngay DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[] { rs.getInt("soDien"), rs.getInt("soNuoc") };
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiSoGanNhat: " + e.getMessage());
        }
        return new int[] { 0, 0 };
    }

    /**
     * Lưu hoặc cập nhật chỉ số điện/nước cho tháng hiện tại.
     * Nếu đã có bản ghi (maPhong, thang, nam) → UPDATE, chưa có → INSERT.
     * 
     * @return null nếu thành công, chuỗi lỗi nếu thất bại
     */
    public String luuHoacCapNhat(ChiSoDienNuoc cs) {
        String sqlCheck = "SELECT 1 FROM ChiSoDienNuoc WHERE maPhong = ? AND thang = ? AND nam = ? AND ngay = ?";
        String sqlUpdate = "UPDATE ChiSoDienNuoc SET soDien = ?, soNuoc = ? "
                + "WHERE maPhong = ? AND thang = ? AND nam = ? AND ngay = ?";
        String sqlInsert = "INSERT INTO ChiSoDienNuoc(maPhong, thang, nam, ngay, soDien, soNuoc) "
                + "VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection con = connectDB.getConnection()) {
            boolean exists;
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setString(1, cs.getMaPhong());
                psCheck.setInt(2, cs.getThang());
                psCheck.setInt(3, cs.getNam());
                psCheck.setInt(4, cs.getNgay());
                try (ResultSet rs = psCheck.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement psUpd = con.prepareStatement(sqlUpdate)) {
                    psUpd.setInt(1, cs.getSoDien());
                    psUpd.setInt(2, cs.getSoNuoc());
                    psUpd.setString(3, cs.getMaPhong());
                    psUpd.setInt(4, cs.getThang());
                    psUpd.setInt(5, cs.getNam());
                    psUpd.setInt(6, cs.getNgay());
                    psUpd.executeUpdate();
                }
            } else {
                try (PreparedStatement psIns = con.prepareStatement(sqlInsert)) {
                    psIns.setString(1, cs.getMaPhong());
                    psIns.setInt(2, cs.getThang());
                    psIns.setInt(3, cs.getNam());
                    psIns.setInt(4, cs.getNgay());
                    psIns.setInt(5, cs.getSoDien());
                    psIns.setInt(6, cs.getSoNuoc());
                    psIns.executeUpdate();
                }
            }
            return null;
        } catch (SQLException e) {
            return "Lỗi lưu chỉ số điện nước: " + e.getMessage();
        }
    }

}