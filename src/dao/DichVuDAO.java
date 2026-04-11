package dao;

import database.connectDB;
import entity.DichVu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DichVuDAO {
    public GiaDetailDAO giaDetailDAO = new GiaDetailDAO();

    private void ensurePhongDichVuTable(Connection con) throws SQLException {
        String sql = "IF OBJECT_ID('dbo.PhongDichVu', 'U') IS NULL "
                + "BEGIN "
                + "CREATE TABLE dbo.PhongDichVu ("
                + "  maPhong NVARCHAR(20) NOT NULL,"
                + "  maDichVu NVARCHAR(20) NOT NULL,"
                + "  CONSTRAINT PK_PhongDichVu PRIMARY KEY(maPhong, maDichVu),"
                + "  CONSTRAINT FK_PhongDichVu_Phong FOREIGN KEY(maPhong) REFERENCES dbo.Phong(maPhong),"
                + "  CONSTRAINT FK_PhongDichVu_DichVu FOREIGN KEY(maDichVu) REFERENCES dbo.DichVu(maDichVu)"
                + ");"
                + "END";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.execute();
        }
    }

    public List<DichVu> layTatCa() {
        String sql = "SELECT d.maDichVu, d.tenDichVu, d.donVi, d.maGiaDetail, gd.donGia " +
                "FROM DichVu d " +
                "LEFT JOIN GiaDetail gd ON d.maGiaDetail = gd.maGiaDetail " +
                "ORDER BY d.tenDichVu";

        List<DichVu> ds = new ArrayList<>();

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DichVu dv = new DichVu(
                            rs.getString("maDichVu"),
                            rs.getString("tenDichVu"),
                            rs.getString("donVi"),
                            rs.getString("maGiaDetail"),
                            rs.getObject("donGia") != null ? rs.getDouble("donGia") : null);
                    if (!dv.getMaDichVu().equals("DV00")) {
                        ds.add(dv);
                    }        
                    
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTatCa DichVu: " + e.getMessage());
        }

        return ds;
    }

    public boolean insertDichVu(DichVu dv) {
        String sqlCount = "SELECT COUNT(*) FROM DichVu";
        String sqlInsert = "INSERT INTO DichVu (maDichVu, tenDichVu, donVi) VALUES (?, ?, ?)";

        try {
            Connection con = connectDB.getConnection();
            con.setAutoCommit(true); // Ensure autocommit is enabled

            // 1. Phát sinh mã
            int count = 0;
            try (PreparedStatement psCount = con.prepareStatement(sqlCount);
                    ResultSet rsCount = psCount.executeQuery()) {
                if (rsCount.next()) {
                    count = rsCount.getInt(1);
                }
            }
            String newMa = String.format("DV%02d", count + 1);
            dv.setMaDichVu(newMa);

            // 2. Insert
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                psInsert.setString(1, dv.getMaDichVu());
                psInsert.setString(2, dv.getTenDichVu());
                psInsert.setString(3, dv.getDonVi());
                return psInsert.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi insert DichVu: " + e.getMessage());
            return false;
        }
    }

    public boolean updateDichVu(DichVu dv) {
        String sql = "UPDATE DichVu SET tenDichVu = ?, donVi = ? WHERE maDichVu = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, dv.getTenDichVu());
                ps.setString(2, dv.getDonVi());
                ps.setString(3, dv.getMaDichVu());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi update DichVu: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteDichVu(String maDichVu) {
        String sql = "DELETE FROM DichVu WHERE maDichVu = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maDichVu);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi delete DichVu: " + e.getMessage());
            return false;
        }
    }

    // Đổi kiểu trả về từ boolean sang đối tượng DichVu
    public DichVu getDichVuByTen(String tenDichVu) {
        DichVu dv = null; // Mặc định là null nếu không tìm thấy

        // Lấy toàn bộ cột của dịch vụ
        String sql = "SELECT * FROM DichVu WHERE tenDichVu = ? AND maDichVu <> 'DV00'";

        try (Connection con = connectDB.getConnection();
                PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, tenDichVu);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Lấy dữ liệu từ các cột
                    String maDichVu = rs.getString("maDichVu");
                    String tenDV = rs.getString("tenDichVu");
                    String donVi = rs.getString("donVi");
                    String maGiaDetail = rs.getString("maGiaDetail");

                    double donGia = giaDetailDAO.getDonGiaByMa(maGiaDetail).getDonGia();

                    // Khởi tạo đối tượng (Đảm bảo class DichVu có constructor này)
                    dv = new DichVu(maDichVu, tenDV, donVi, maGiaDetail, donGia);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Trả về đối tượng tìm được (hoặc null nếu không có)
        return dv;
    }

    public Set<String> layMaDichVuTheoPhong(String maPhong) {
        Set<String> result = new HashSet<>();
        String sql = "SELECT maDichVu FROM PhongDichVu WHERE maPhong = ? AND maDichVu <> 'DV00'";

        try (Connection con = connectDB.getConnection()) {
            ensurePhongDichVuTable(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maPhong);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(rs.getString("maDichVu"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layMaDichVuTheoPhong: " + e.getMessage());
        }
        return result;
    }

    public boolean capNhatDichVuPhong(String maPhong, Set<String> dsMaDichVu) {
        String sqlDelete = "DELETE FROM PhongDichVu WHERE maPhong = ?";
        String sqlInsert = "INSERT INTO PhongDichVu(maPhong, maDichVu) VALUES (?, ?)";

        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            ensurePhongDichVuTable(con);

            try (PreparedStatement psDel = con.prepareStatement(sqlDelete)) {
                psDel.setString(1, maPhong);
                psDel.executeUpdate();
            }

            if (dsMaDichVu != null && !dsMaDichVu.isEmpty()) {
                try (PreparedStatement psIns = con.prepareStatement(sqlInsert)) {
                    for (String maDv : dsMaDichVu) {
                        psIns.setString(1, maPhong);
                        psIns.setString(2, maDv);
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatDichVuPhong: " + e.getMessage());
            return false;
        }
    }

    public void ganTatCaDichVuChoPhongNeuChuaCo(String maPhong) {
        String sqlCount = "SELECT COUNT(1) AS cnt FROM PhongDichVu WHERE maPhong = ?";
        String sqlInsertAll = "INSERT INTO PhongDichVu(maPhong, maDichVu) "
                + "SELECT ?, maDichVu FROM DichVu WHERE maDichVu <> 'DV00'";

        try (Connection con = connectDB.getConnection()) {
            ensurePhongDichVuTable(con);

            int count = 0;
            try (PreparedStatement psCount = con.prepareStatement(sqlCount)) {
                psCount.setString(1, maPhong);
                try (ResultSet rs = psCount.executeQuery()) {
                    if (rs.next()) {
                        count = rs.getInt("cnt");
                    }
                }
            }

            if (count == 0) {
                try (PreparedStatement psIns = con.prepareStatement(sqlInsertAll)) {
                    psIns.setString(1, maPhong);
                    psIns.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi ganTatCaDichVuChoPhongNeuChuaCo: " + e.getMessage());
        }
    }

}