package dao;

import database.connectDB;
import entity.GiaDetail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GiaDetailDAO {

    public List<GiaDetail> layTheoHeader(String maGiaHeader) {
        String sql = "SELECT maGiaDetail, maGiaHeader, loaiPhong, maDichVu, donGia "
                + "FROM GiaDetail WHERE maGiaHeader = ? ORDER BY maGiaDetail";
        List<GiaDetail> ds = new ArrayList<>();

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maGiaHeader);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Integer loaiPhong = rs.getObject("loaiPhong") == null
                                ? null
                                : rs.getInt("loaiPhong");
                        ds.add(new GiaDetail(
                                rs.getString("maGiaDetail"),
                                rs.getString("maGiaHeader"),
                                loaiPhong,
                                rs.getString("maDichVu"),
                                rs.getDouble("donGia")));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTheoHeader GiaDetail: " + e.getMessage());
        }

        return ds;
    }

    public boolean thayTheChiTiet(Connection con, String maGiaHeader, List<GiaDetail> details) throws SQLException {
        try (PreparedStatement psDel = con.prepareStatement("DELETE FROM GiaDetail WHERE maGiaHeader = ?")) {
            psDel.setString(1, maGiaHeader);
            psDel.executeUpdate();
        }

        String sqlInsert = "INSERT INTO GiaDetail(maGiaDetail, maGiaHeader, loaiPhong, maDichVu, donGia) "
                + "VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement psIns = con.prepareStatement(sqlInsert)) {
            int idx = 1;
            for (GiaDetail d : details) {
                String maDetail = d.getMaGiaDetail();
                if (maDetail == null || maDetail.isBlank()) {
                    maDetail = phatSinhMaChiTiet(maGiaHeader, idx++);
                }

                psIns.setString(1, maDetail);
                psIns.setString(2, maGiaHeader);
                if (d.getLoaiPhong() == null) {
                    psIns.setNull(3, java.sql.Types.TINYINT);
                } else {
                    psIns.setInt(3, d.getLoaiPhong());
                }
                psIns.setString(4, d.getMaDichVu());
                psIns.setDouble(5, d.getDonGia());
                psIns.addBatch();
            }
            psIns.executeBatch();
        }
        return true;
    }

    private String phatSinhMaChiTiet(String maHeader, int idx) {
        return "GD" + maHeader + "_" + idx + "_" + (System.nanoTime() % 100000);
    }

    public GiaDetail getDonGiaByMa (String maDonGia){
        String sql = "SELECT d.* FROM GiaDetail d " +
                "JOIN GiaHeader h ON d.maGiaHeader = h.maGiaHeader " +
                "WHERE d.maGiaDetail = ? AND h.trangThai = 1";
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maDonGia);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Khởi tạo đối tượng và trả về
                return new GiaDetail(
                        rs.getString("maGiaDetail"),
                        rs.getString("maGiaHeader"),
                        rs.getInt("loaiPhong"),
                        rs.getString("maDichVu"),
                        rs.getDouble("donGia")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
