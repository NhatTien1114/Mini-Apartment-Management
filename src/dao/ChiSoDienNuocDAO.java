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
     * @return int[]{soDien, soNuoc}
     */
    public ArrayList<ChiSoDienNuoc> getAllChiSoThang(String thangHienTai, String namHienTai) {
        ArrayList<ChiSoDienNuoc> dsChiSo = new ArrayList<>();

        String sql = "SELECT * FROM ChiSoDienNuoc WHERE thang = ? AND nam = ? ORDER BY maPhong ASC";

        try (Connection con = connectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, thangHienTai);
            stmt.setString(2, namHienTai);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String maPhong = rs.getString("maPhong");
                    int thang = rs.getInt("thang");
                    int nam = rs.getInt("nam");
                    int soDien = rs.getInt("soDien");
                    int soNuoc = rs.getInt("soNuoc");

                    ChiSoDienNuoc csdn = new ChiSoDienNuoc(maPhong, thang, nam, soDien, soNuoc);

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
                    int soDien = rs.getInt("soDien");
                    int soNuoc = rs.getInt("soNuoc");

                    ChiSoDienNuoc csdn = new ChiSoDienNuoc(maPhong, thang, nam, soDien, soNuoc);
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
                + "ORDER BY nam DESC, thang DESC";
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ps.setInt(2, namHienTai);
            ps.setInt(3, namHienTai);
            ps.setInt(4, thangHienTai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[]{ rs.getInt("soDien"), rs.getInt("soNuoc") };
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiSoThangTruoc: " + e.getMessage());
        }
        return new int[]{ 0, 0 }; // Tháng đầu tiên → 0
    }

    /**
     * Lưu hoặc cập nhật chỉ số điện/nước cho tháng hiện tại.
     * Nếu đã có bản ghi (maPhong, thang, nam) → UPDATE, chưa có → INSERT.
     * @return null nếu thành công, chuỗi lỗi nếu thất bại
     */
    public String luuHoacCapNhat(ChiSoDienNuoc cs) {
        String sqlCheck = "SELECT 1 FROM ChiSoDienNuoc WHERE maPhong = ? AND thang = ? AND nam = ?";
        String sqlUpdate = "UPDATE ChiSoDienNuoc SET soDien = ?, soNuoc = ? "
                + "WHERE maPhong = ? AND thang = ? AND nam = ?";
        String sqlInsert = "INSERT INTO ChiSoDienNuoc(maPhong, thang, nam, soDien, soNuoc) "
                + "VALUES(?, ?, ?, ?, ?)";

        try (Connection con = connectDB.getConnection()) {
            boolean exists;
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setString(1, cs.getMaPhong());
                psCheck.setInt(2, cs.getThang());
                psCheck.setInt(3, cs.getNam());
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
                    psUpd.executeUpdate();
                }
            } else {
                try (PreparedStatement psIns = con.prepareStatement(sqlInsert)) {
                    psIns.setString(1, cs.getMaPhong());
                    psIns.setInt(2, cs.getThang());
                    psIns.setInt(3, cs.getNam());
                    psIns.setInt(4, cs.getSoDien());
                    psIns.setInt(5, cs.getSoNuoc());
                    psIns.executeUpdate();
                }
            }
            return null;
        } catch (SQLException e) {
            return "Lỗi lưu chỉ số điện nước: " + e.getMessage();
        }
    }

}